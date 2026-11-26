package org.example.shopping.order;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.shopping.orderItem.OrderItem;
import org.example.shopping.payment.Payment;
import org.example.shopping.users.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "order_tb")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order")
    private Payment payment;

    private LocalDateTime createdAt;

    @Builder
    public Order(Long id, User user, String orderNumber, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.orderNumber = orderNumber;
        this.createdAt = createdAt;
    }

    public void addItem(OrderItem item) {
        this.orderItems.add(item);
    }
}
