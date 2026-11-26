package org.example.shopping.payment.paymentEnum;


/**
 * 결제 상태(Payment Status) - 생명주기 관리
 *
 *      READY => (PG 승인 성공) => SUCCESS
 *      READY => (PG 승인 실패) => FAILED
 *      SUCCESS => (일부 상품 환불) => PARTIAL_REFUNDED
 *      SUCCESS / PARTIAL_REFUNDED => (남은 금액까지 환불) => REFUNDED
 *
 *      구매확정은 상품 단위라 OrderItemStatus 에서 관리
 *
 * */
public enum PaymentStatus {
    SUCCESS,
    FAILED,
    PARTIAL_REFUNDED,
    REFUNDED
}
