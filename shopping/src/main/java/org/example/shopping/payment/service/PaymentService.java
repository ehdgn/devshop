package org.example.shopping.payment.service;

import lombok.RequiredArgsConstructor;
import org.example.shopping._core.errors.exception.Exception400;
import org.example.shopping._core.errors.exception.Exception403;
import org.example.shopping._core.errors.exception.Exception404;
import org.example.shopping.cart.Cart;
import org.example.shopping.cart.CartRepository;
import org.example.shopping.cartItem.CartItem;
import org.example.shopping.cartItem.CartItemRepository;
import org.example.shopping.order.Order;
import org.example.shopping.order.OrderService;
import org.example.shopping.orderItem.OrderItem;
import org.example.shopping.orderItem.OrderItemRepository;
import org.example.shopping.orderItem.OrderItemStatus;
import org.example.shopping.payment.Payment;
import org.example.shopping.payment.PaymentRefund;
import org.example.shopping.payment.PaymentRefundRepository;
import org.example.shopping.payment.PaymentRepository;
import org.example.shopping.payment.dto.PaymentRequest;
import org.example.shopping.payment.dto.PaymentResponse;
import org.example.shopping.payment.error.BusinessException;
import org.example.shopping.payment.error.ErrorCode;
import org.example.shopping.payment.log.History;
import org.example.shopping.payment.log.HistoryRepository;
import org.example.shopping.payment.paymentEnum.PaymentMethod;
import org.example.shopping.payment.paymentEnum.RefundStatus;
import org.example.shopping.payment.service.gateway.PaymentGateway;
import org.example.shopping.payment.service.gateway.PaymentGatewayResolver;
import org.example.shopping.payment.service.gateway.PaymentResult;
import org.example.shopping.product.Product;
import org.example.shopping.product.ProductRepository;
import org.example.shopping.users.MailService;
import org.example.shopping.users.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {
    private final PaymentRefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayResolver gatewayResolver;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;
    private final OrderItemRepository orderItemRepository;
    private final HistoryRepository historyRepository;
    private final ProductRepository productRepository;
    private final MailService mailService;


    // ================== 카트 정보 가져오기 ================

    public PaymentResponse.CartPaymentDTO getCartInfo(Long cartId, Long userId) {
        List<CartItem> checkItem = getChecked(cartId, userId);
        List<PaymentResponse.CartItemPaymentDTO> itemsInfo = checkItem.stream()
                .map(item -> new PaymentResponse.CartItemPaymentDTO(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getProductCode(),
                        item.getProduct().getProductName(),
                        item.getQuantity(),
                        item.getProduct().getPrice(),
                        item.getTotalPrice()
                ))
                .toList();

        return new PaymentResponse.CartPaymentDTO(cartId, itemsInfo, totalOf(checkItem));
    }

    // cartCheck 검증 편의
    private List<CartItem> getChecked(Long cartId, Long userId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new Exception404("장바구니를 찾을 수 없습니다."));
        if (!cart.getUser().getId().equals(userId)) {
            throw new Exception403("본인의 장바구니만 결제할 수 있습니다.");
        }
        List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);
        List<CartItem> checkItem = cartItems.stream()
                .filter(CartItem::isItemChecked)
                .toList();
        if (checkItem.isEmpty()) {
            throw new Exception400("결제할 상품을 선택해 주세요");
        }
        return checkItem;
    }

    private long totalOf(List<CartItem> items) {
        return items.stream().mapToLong(CartItem::getTotalPrice).sum();
    }

    // ===================== 결제 후 수량 증가 감소 메서드 =====================
    private void reserveStock(List<CartItem> items) {
        for (CartItem item : items) {
            Product product = item.getProduct();
            if (productRepository.decreaseStock(product.getId(), item.getQuantity()) == 0) {
                throw new Exception400("재고가 부족합니다: " + product.getProductName());
            }
        }
    }

    // ===================== 환불 후 수량 증가 감소 메서드 =====================
    @Transactional
    public void increaseQuantity(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new Exception404("NOT FOUND"));
        product.increaseQuantity(quantity);
        productRepository.save(product);
    }

    // ===================== 결제 =====================

    // 목 결제
    @Transactional
    public void createPayment(PaymentRequest.CreateDTO createDTO, User user, Long cartId) {
        if (createDTO.getMethod() != PaymentMethod.MOCK) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "TossPay는 approvePayment 메서드를 사용하세요");
        }
        List<CartItem> checkItem = getChecked(cartId, user.getId());
        reserveStock(checkItem);
        placeOrder(user, checkItem, PaymentMethod.MOCK, "ORD-" + UUID.randomUUID(), "MOCK-" + UUID.randomUUID());
    }

    // toss 결제
    @Transactional
    public PaymentResponse.PaymentResultDTO approvePayment(User sessionUser, Long cartId, PaymentRequest.CreateDTO createDTO) {
        List<CartItem> checkItem = getChecked(cartId, sessionUser.getId());
        // 금액은 브라우저가 보낸 값이라 승인 전에 서버 계산값과 맞춰본다
        if (createDTO.getAmount() == null || createDTO.getAmount() != totalOf(checkItem)) {
            throw new Exception400("결제 금액이 주문 금액과 일치하지 않습니다.");
        }
        // 재고를 먼저 확보한다: 승인 후에 재고가 부족하면 돈만 빠져나가기 때문 (승인 실패 시 롤백으로 복구)
        reserveStock(checkItem);
        PaymentGateway gateway = gatewayResolver.resolve(createDTO.getMethod());
        PaymentResult paymentResult = gateway.approve(createDTO);
        if (!paymentResult.isSuccess()) {
            throw new Exception400("결제 승인에 실패했습니다: " + paymentResult.getFailureMessage());
        }

        Order order = placeOrder(sessionUser, checkItem, createDTO.getMethod(), createDTO.getOrderId(), createDTO.getPaymentKey());

        List<PaymentResponse.PaymentResultDTO.PaymentItemDTO> items = order.getOrderItems().stream()
                .map(item -> {
                    PaymentResponse.PaymentResultDTO.PaymentItemDTO dto =
                            new PaymentResponse.PaymentResultDTO.PaymentItemDTO();
                    dto.setProductName(item.getProductName());
                    dto.setProductCode(item.getProductCode());
                    dto.setTotalPrice(item.getAmount());
                    return dto;
                })
                .toList();
        PaymentResponse.PaymentResultDTO result = new PaymentResponse.PaymentResultDTO();
        result.setItems(items);
        return result;
    }

    // 승인된 결제로 주문 1건 + 주문상품 N개 + 결제 1건을 만든다
    private Order placeOrder(User user, List<CartItem> checkItem, PaymentMethod method, String pgOrderId, String paymentKey) {
        Order order = orderService.createOrder(user.getId());
        for (CartItem cartItem : checkItem) {
            order.addItem(new OrderItem(order, cartItem.getProduct(), cartItem.getQuantity()));
        }

        Payment payment = Payment.builder()
                .user(order.getUser())
                .order(order)
                .pgOrderId(pgOrderId)
                .paymentKey(paymentKey)
                .amount(totalOf(checkItem))
                .method(method)
                .build();
        payment.paySuccess();
        paymentRepository.save(payment);
        order.setPayment(payment);

        orderItemRepository.saveAll(order.getOrderItems());
        order.getOrderItems().forEach(item -> historyRepository.save(new History(item, payment)));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new Exception404("장바구니를 찾을 수 없습니다."));
        cart.removeCheckedItem();
        cart.updateAmount();

        mailService.sendPayInfo(order.getUser(), order.getId());
        return order;
    }

    // ===================== 환불 / 구매확정 (주문상품 단위) =====================

    @Transactional
    public PaymentResponse.SingleRefundDTO refund(Long orderItemId, Long userId, PaymentRequest.RefundDTO req) {
        OrderItem item = getOwnedPaidItem(orderItemId, userId);
        Payment payment = item.getOrder().getPayment();
        String reason = (req.getReason() != null) ? req.getReason() : "고객요청";

        // 상품 1개만 부분취소: 금액은 주문 당시 이 상품의 금액
        PaymentRequest.RefundDTO refundDTO = new PaymentRequest.RefundDTO();
        refundDTO.setPaymentKey(payment.getPaymentKey());
        refundDTO.setAmount(item.getAmount());
        refundDTO.setReason(reason);

        PaymentGateway gateway = gatewayResolver.resolve(payment.getMethod());
        PaymentResult result = gateway.refund(refundDTO);

        PaymentRefund refund = PaymentRefund.builder()
                .user(payment.getUser())
                .payment(payment)
                .orderItem(item)
                .amount(item.getAmount())
                .reason(reason)
                .status(RefundStatus.REQUESTED)
                .requestedAt(LocalDateTime.now())
                .build();

        if (result.isSuccess()) {
            refund.refundCompleted();
            payment.refund(item.getAmount());
            item.refund();
            increaseQuantity(item.getProduct().getId(), item.getQuantity());
            recordStatus(item, reason);
        } else {
            refund.refundFailed(result.getFailureCode(), result.getFailureMessage());
        }
        refundRepository.save(refund);

        return new PaymentResponse.SingleRefundDTO(refund);
    }

    @Transactional
    public void confirmPurchase(Long orderItemId, Long userId) {
        OrderItem item = getOwnedPaidItem(orderItemId, userId);
        item.confirm();
        recordStatus(item, null);
    }

    // 본인 주문이고, 아직 환불·구매확정 전(PAID)일 때만 상태를 바꿀 수 있다
    private OrderItem getOwnedPaidItem(Long orderItemId, Long userId) {
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new Exception404("주문 상품을 찾을 수 없습니다."));
        if (!item.isOrderedBy(userId)) {
            throw new Exception403("본인의 주문만 처리할 수 있습니다.");
        }
        if (item.getStatus() != OrderItemStatus.PAID) {
            throw new Exception400("이미 처리된 주문 상품입니다.");
        }
        return item;
    }

    private void recordStatus(OrderItem item, String reason) {
        historyRepository.findByOrderItemId(item.getId())
                .ifPresent(history -> history.changeStatus(item.getStatus().name(), reason));
    }
}
