package org.example.shopping.order;

import lombok.RequiredArgsConstructor;
import org.example.shopping._core.errors.exception.Exception400;
import org.example.shopping._core.errors.exception.Exception403;
import org.example.shopping._core.errors.exception.Exception404;
import org.example.shopping._core.utils.MyDateUtil;
import org.example.shopping.users.User;
import org.example.shopping.users.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public Order createOrder(Long userId) {
        User userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));

        Order order = Order.builder()
                .user(userEntity)
                .orderNumber(createOrderNumber())
                .createdAt(LocalDateTime.now())
                .build();

       return orderRepository.save(order);
    }

    public List<OrderResponse.OrderListDTO> orderList(Long userId, String keyword) {

        if (keyword != null && !keyword.trim().isEmpty()) {
            List<Order> orderList = orderRepository.findByKeyword(userId, keyword.trim());
            return orderList.stream()
                    .map(OrderResponse.OrderListDTO::new)
                    .toList();
        } else {
            List<Order> orderList = orderRepository.findAllByUserIdAndOrderByCreatedAtDesc(userId);
            return orderList.stream()
                    .map(OrderResponse.OrderListDTO::new)
                    .toList();
        }
    }

    public OrderResponse.OrderDetailDTO orderDetail(Long orderId, Long userId) {
        if (orderId == null) {
            throw new Exception400("주문 번호가 없습니다.");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new Exception404("주문을 찾을 수 없습니다."));
        if (!order.getUser().getId().equals(userId)) {
            throw new Exception403("본인의 주문만 볼 수 있습니다.");
        }

        return new OrderResponse.OrderDetailDTO(order);
    }

    private String createOrderNumber() {
        String orderNumber = UUID.randomUUID().toString();
        orderNumber = orderNumber.replaceAll("-","").substring(0, 8);
        orderNumber = orderNumber.toUpperCase();
        return MyDateUtil.toOrderString(LocalDateTime.now()) + orderNumber;
    }
}
