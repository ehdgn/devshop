package org.example.shopping.payment.service;

import org.example.shopping._core.errors.exception.Exception400;
import org.example.shopping._core.errors.exception.Exception403;
import org.example.shopping.cart.Cart;
import org.example.shopping.cart.CartRepository;
import org.example.shopping.cartItem.CartItem;
import org.example.shopping.cartItem.CartItemRepository;
import org.example.shopping.payment.Payment;
import org.example.shopping.payment.PaymentRepository;
import org.example.shopping.payment.dto.PaymentRequest;
import org.example.shopping.payment.paymentEnum.PaymentMethod;
import org.example.shopping.payment.paymentEnum.PaymentStatus;
import org.example.shopping.payment.service.gateway.PaymentGatewayResolver;
import org.example.shopping.product.Product;
import org.example.shopping.users.User;
import org.example.shopping.users.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock PaymentRepository paymentRepository;
    @Mock UserRepository userRepository;
    @Mock CartRepository cartRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock PaymentGatewayResolver gatewayResolver;
    @InjectMocks PaymentService paymentService;

    User owner = User.builder().id(1L).username("buyer").build();
    User other = User.builder().id(2L).username("other").build();

    private Payment payment(PaymentStatus status) {
        return Payment.builder().id(10L).user(owner).amount(5000L).status(status).method(PaymentMethod.MOCK).build();
    }

    @Test
    void 남의_결제는_환불할_수_없다() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(other));
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment(PaymentStatus.SUCCESS)));

        assertThrows(Exception403.class,
                () -> paymentService.singleRefund(10L, 2L, new PaymentRequest.RefundDTO()));
        verifyNoInteractions(gatewayResolver);
    }

    @Test
    void 이미_환불된_결제는_다시_환불할_수_없다() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment(PaymentStatus.REFUNDED)));

        assertThrows(Exception400.class,
                () -> paymentService.singleRefund(10L, 1L, new PaymentRequest.RefundDTO()));
        verifyNoInteractions(gatewayResolver);
    }

    @Test
    void 남의_결제는_구매확정할_수_없다() {
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment(PaymentStatus.SUCCESS)));

        assertThrows(Exception403.class, () -> paymentService.confirmPurchase(10L, 2L));
    }

    @Test
    void 남의_장바구니로는_결제할_수_없다() {
        when(cartRepository.findById(7L)).thenReturn(Optional.of(new Cart(owner)));

        assertThrows(Exception403.class, () -> paymentService.getCartInfo(7L, 2L));
    }

    @Test
    void 조작된_금액이면_토스_승인을_요청하지_않는다() {
        Product product = mock(Product.class);
        when(product.getPrice()).thenReturn(10_000L);
        when(cartRepository.findById(7L)).thenReturn(Optional.of(new Cart(owner)));
        CartItem item = CartItem.builder().product(product).quantity(2).build();
        when(cartItemRepository.findByCartId(7L)).thenReturn(List.of(item));

        PaymentRequest.CreateDTO dto = new PaymentRequest.CreateDTO();
        dto.setMethod(PaymentMethod.TOSS_PAY);
        dto.setAmount(1_000L); // 실제 합계는 20,000원

        assertThrows(Exception400.class, () -> paymentService.approvePayment(owner, 7L, dto));
        verify(gatewayResolver, never()).resolve(any());
    }
}
