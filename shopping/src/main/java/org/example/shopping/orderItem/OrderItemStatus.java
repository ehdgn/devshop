package org.example.shopping.orderItem;

/**
 * 주문 상품 상태
 *
 *      PAID => (구매확정) => CONFIRMED
 *      PAID => (환불 완료) => REFUNDED
 */
public enum OrderItemStatus {
    PAID,
    CONFIRMED,
    REFUNDED
}
