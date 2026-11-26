package org.example.shopping.order;

import org.example.shopping._core.errors.exception.Exception403;
import org.example.shopping.users.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @InjectMocks OrderService orderService;

    @Test
    void 남의_주문_상세는_볼_수_없다() {
        Order order = Order.builder().id(5L).user(User.builder().id(1L).build()).build();
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        assertThrows(Exception403.class, () -> orderService.orderDetail(5L, 2L));
    }
}
