package org.example.shopping.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.shopping.payment.PaymentRefund;
import org.example.shopping.payment.paymentEnum.PaymentMethod;
import org.example.shopping.payment.paymentEnum.RefundStatus;

import java.util.List;

public class PaymentResponse {

    @Data
    @AllArgsConstructor
    public static class CartPaymentDTO {
        private Long cartId;
        private List<CartItemPaymentDTO> items;
        private Long totalAmount;
    }

    @Data
    @AllArgsConstructor
    public static class CartItemPaymentDTO {
        private Long cartItemId;
        private Long productId;
        private String productCode;
        private String productName;
        private Integer quantity;
        private Long amount;
        private Long totalPrice;
    }

    @Data
    public static class PaymentResultDTO{
        private List<PaymentItemDTO> items;

        @Data
        public static class PaymentItemDTO{
            private String productName;
            private String productCode;
            private Long totalPrice;
        }
    }


    @Data
    public static class SingleRefundDTO {
        private Long userId;
        private Long orderItemId;
        private String paymentKey;
        private Long amount;
        PaymentMethod method;
        RefundStatus status;
        private String productCode;
        private String productName;

        public SingleRefundDTO(PaymentRefund refund){
            this.userId = refund.getUser().getId();
            this.orderItemId = refund.getOrderItem().getId();
            this.paymentKey = refund.getPayment().getPaymentKey();
            this.amount = refund.getAmount();
            this.method = refund.getPayment().getMethod();
            this.status = refund.getStatus();
            this.productCode = refund.getOrderItem().getProductCode();
            this.productName = refund.getOrderItem().getProductName();
        }

    }


}
