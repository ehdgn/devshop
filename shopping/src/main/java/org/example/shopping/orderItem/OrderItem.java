package org.example.shopping.orderItem;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.shopping.order.Order;
import org.example.shopping.product.Product;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "order_item_tb")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    // 주문 당시 정보: 이후 상품명·가격이 바뀌어도 주문 내역은 그대로 남아야 함
    private String productName;
    private String productCode;
    private Long price;
    private Integer quantity;
    private Long amount;

    @Enumerated(EnumType.STRING)
    private OrderItemStatus status;

    private LocalDateTime confirmedAt;
    private LocalDateTime refundedAt;

    public OrderItem(Order order, Product product, Integer quantity) {
        this.order = order;
        this.product = product;
        this.productName = product.getProductName();
        this.productCode = product.getProductCode();
        this.price = product.getPrice();
        this.quantity = quantity;
        this.amount = product.getPrice() * quantity;
        this.status = OrderItemStatus.PAID;
    }

    public boolean isOrderedBy(Long userId) {
        return this.order.getUser().getId().equals(userId);
    }

    public void confirm() {
        this.status = OrderItemStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void refund() {
        this.status = OrderItemStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
    }
}
