package org.example.shopping.payment.log;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.shopping.orderItem.OrderItem;
import org.example.shopping.payment.Payment;
import org.example.shopping.payment.paymentEnum.PaymentMethod;
import org.example.shopping.users.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

// 주문 상품 1개당 1행: 상태가 바뀌면 같은 행의 old/new 값을 갱신한다 (관리자 통계용)
@NoArgsConstructor
@Entity
@Table(name = "history_tb")
@Getter
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", unique = true)
    private OrderItem orderItem;

    private String old_value;

    private String new_value;

    private String username;

    private String email;

    private Long productId;

    private String productCode;

    private String productName;

    private Long amount;

    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    private Field field;

    private String reason;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;
    @UpdateTimestamp
    private Timestamp updateAt;

    public History(OrderItem item, Payment payment) {
        User user = payment.getUser();
        this.user = user;
        this.payment = payment;
        this.orderItem = item;
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.productId = item.getProduct().getId();
        this.productCode = item.getProductCode();
        this.productName = item.getProductName();
        this.amount = item.getAmount();
        this.quantity = item.getQuantity();
        this.method = payment.getMethod();
        this.field = Field.STATUS;
        this.new_value = item.getStatus().name();
    }

    public void changeStatus(String newValue, String reason) {
        this.old_value = this.new_value;
        this.new_value = newValue;
        this.reason = reason;
    }
}
