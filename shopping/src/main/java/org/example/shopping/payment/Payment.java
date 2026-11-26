package org.example.shopping.payment;

import jakarta.persistence.*;
import lombok.*;
import org.example.shopping._core.utils.BaseTimeEntity;
import org.example.shopping.order.Order;
import org.example.shopping.payment.paymentEnum.PaymentMethod;
import org.example.shopping.payment.paymentEnum.PaymentStatus;
import org.example.shopping.users.User;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// 주문 1건에 대한 실제 결제 1건 (PG의 paymentKey 하나와 1:1)
@Entity
@Table(name = "payments_tb")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Payment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    // PG에 보낸 주문 ID (토스 orderId)
    private String pgOrderId;

    @Column(unique = true)
    private String paymentKey;

    private Long amount;

    @Builder.Default
    private Long refundedAmount = 0L;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String failureCode;

    private String failureMessage;

    @CreationTimestamp
    private LocalDateTime requestedAt;

    private LocalDateTime approvedAt;

    private LocalDateTime cancelledAt;


    // 결제 성공
    public void paySuccess() {
        this.status = PaymentStatus.SUCCESS;
        this.approvedAt = LocalDateTime.now();
        this.failureCode = null;
        this.failureMessage = null;
    }

    // 결제 실패 처리
    public void payFailed(String code, String message) {
        this.status = PaymentStatus.FAILED;
        this.failureCode = code;
        this.failureMessage = message;
    }

    // 환불 처리: 누적 환불액이 결제액에 닿으면 전액 환불
    public void refund(Long refundAmount) {
        this.refundedAmount += refundAmount;
        this.status = this.refundedAmount >= this.amount ? PaymentStatus.REFUNDED : PaymentStatus.PARTIAL_REFUNDED;
        this.cancelledAt = LocalDateTime.now();
    }

    public Long getRemainingAmount() {
        return this.amount - this.refundedAmount;
    }
}
