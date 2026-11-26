package org.example.shopping.payment.service;

import org.example.shopping._core.errors.exception.Exception400;
import org.example.shopping._core.errors.exception.Exception403;
import org.example.shopping.cart.Cart;
import org.example.shopping.cart.CartRepository;
import org.example.shopping.cartItem.CartItem;
import org.example.shopping.cartItem.CartItemRepository;
import org.example.shopping.order.Order;
import org.example.shopping.orderItem.OrderItem;
import org.example.shopping.orderItem.OrderItemRepository;
import org.example.shopping.orderItem.OrderItemStatus;
import org.example.shopping.payment.Payment;
import org.example.shopping.payment.PaymentRefundRepository;
import org.example.shopping.payment.dto.PaymentRequest;
import org.example.shopping.payment.log.HistoryRepository;
import org.example.shopping.payment.paymentEnum.PaymentMethod;
import org.example.shopping.payment.paymentEnum.PaymentStatus;
import org.example.shopping.payment.service.gateway.PaymentGateway;
import org.example.shopping.payment.service.gateway.PaymentGatewayResolver;
import org.example.shopping.payment.service.gateway.PaymentResult;
import org.example.shopping.product.Product;
import org.example.shopping.product.ProductRepository;
import org.example.shopping.users.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock OrderItemRepository orderItemRepository;
    @Mock PaymentRefundRepository refundRepository;
    @Mock HistoryRepository historyRepository;
    @Mock ProductRepository productRepository;
    @Mock CartRepository cartRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock PaymentGatewayResolver gatewayResolver;
    @Mock PaymentGateway gateway;
    @InjectMocks PaymentService paymentService;

    User owner = User.builder().id(1L).username("buyer").build();

    // 상품 2개(10,000원 x1, 5,000원 x2)를 20,000원에 결제한 주문의 첫 번째 상품
    private OrderItem paidItem() {
        Order order = Order.builder().user(owner).build();
        OrderItem first = new OrderItem(order, product(10_000L), 1);
        order.addItem(first);
        order.addItem(new OrderItem(order, product(5_000L), 2));
        Payment payment = Payment.builder().user(owner).order(order).amount(20_000L)
                .paymentKey("pk").method(PaymentMethod.TOSS_PAY).build();
        payment.paySuccess();
        order.setPayment(payment);
        return first;
    }

    private Product product(Long price) {
        Product product = mock(Product.class);
        lenient().when(product.getPrice()).thenReturn(price);
        return product;
    }

    @Test
    void 남의_주문상품은_환불할_수_없다() {
        OrderItem item = paidItem();
        when(orderItemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThrows(Exception403.class,
                () -> paymentService.refund(10L, 2L, new PaymentRequest.RefundDTO()));
        verifyNoInteractions(gatewayResolver);
    }

    @Test
    void 이미_환불된_상품은_다시_환불할_수_없다() {
        OrderItem item = paidItem();
        item.refund();
        when(orderItemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThrows(Exception400.class,
                () -> paymentService.refund(10L, 1L, new PaymentRequest.RefundDTO()));
        verifyNoInteractions(gatewayResolver);
    }

    @Test
    void 상품_하나를_환불하면_그_금액만_부분취소된다() {
        OrderItem item = paidItem();
        when(orderItemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(gatewayResolver.resolve(PaymentMethod.TOSS_PAY)).thenReturn(gateway);
        when(gateway.refund(any())).thenReturn(PaymentResult.ok("pk"));
        when(productRepository.findById(any())).thenReturn(Optional.of(item.getProduct()));

        paymentService.refund(10L, 1L, new PaymentRequest.RefundDTO());

        ArgumentCaptor<PaymentRequest.RefundDTO> sent = ArgumentCaptor.forClass(PaymentRequest.RefundDTO.class);
        verify(gateway).refund(sent.capture());
        assertEquals(10_000L, sent.getValue().getAmount());
        assertEquals(OrderItemStatus.REFUNDED, item.getStatus());
        Payment payment = item.getOrder().getPayment();
        assertEquals(PaymentStatus.PARTIAL_REFUNDED, payment.getStatus());
        assertEquals(10_000L, payment.getRemainingAmount());
        verify(item.getProduct()).increaseQuantity(1);
    }

    @Test
    void 남의_주문상품은_구매확정할_수_없다() {
        OrderItem item = paidItem();
        when(orderItemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThrows(Exception403.class, () -> paymentService.confirmPurchase(10L, 2L));
    }

    @Test
    void 남의_장바구니로는_결제할_수_없다() {
        when(cartRepository.findById(7L)).thenReturn(Optional.of(new Cart(owner)));

        assertThrows(Exception403.class, () -> paymentService.getCartInfo(7L, 2L));
    }

    @Test
    void 조작된_금액이면_토스_승인을_요청하지_않는다() {
        Product product = product(10_000L);
        when(cartRepository.findById(7L)).thenReturn(Optional.of(new Cart(owner)));
        CartItem item = CartItem.builder().product(product).quantity(2).build();
        when(cartItemRepository.findByCartId(7L)).thenReturn(List.of(item));

        PaymentRequest.CreateDTO dto = new PaymentRequest.CreateDTO();
        dto.setMethod(PaymentMethod.TOSS_PAY);
        dto.setAmount(1_000L); // 실제 합계는 20,000원

        assertThrows(Exception400.class, () -> paymentService.approvePayment(owner, 7L, dto));
        verify(gatewayResolver, never()).resolve(any());
    }

    @Test
    void 재고가_부족하면_토스_승인을_요청하지_않는다() {
        Product product = product(10_000L);
        when(product.getId()).thenReturn(5L);
        when(cartRepository.findById(7L)).thenReturn(Optional.of(new Cart(owner)));
        CartItem item = CartItem.builder().product(product).quantity(2).build();
        when(cartItemRepository.findByCartId(7L)).thenReturn(List.of(item));
        when(productRepository.decreaseStock(5L, 2)).thenReturn(0); // 조건(재고 >= 수량)에 맞는 행 없음

        PaymentRequest.CreateDTO dto = new PaymentRequest.CreateDTO();
        dto.setMethod(PaymentMethod.TOSS_PAY);
        dto.setAmount(20_000L);

        assertThrows(Exception400.class, () -> paymentService.approvePayment(owner, 7L, dto));
        verify(gatewayResolver, never()).resolve(any());
    }
}
