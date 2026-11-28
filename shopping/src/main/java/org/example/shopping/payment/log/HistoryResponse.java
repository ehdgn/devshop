package org.example.shopping.payment.log;

import lombok.Data;

public class HistoryResponse {
    @Data
    public static class ListDTO {
        private Long id;
        private Long userId;
        private String username;
        private String email;
        private Long paymentId;
        private Long productId;
        private String productName;
        private String productCode;
        private Integer quantity;
        private Long amount;
        private String method;
        private String oldValue;
        private String newValue;
        private String reason;
        private String createdAt;
        private String updatedAt;
        private String createdAtText;
        private String amountText;
        private String statusLabel;

        public ListDTO(History history) {
            this.id = history.getId();
            this.userId = history.getUser().getId();
            this.username = history.getUsername();
            this.email = history.getEmail();
            this.paymentId = history.getPayment().getId();
            this.productId = history.getProductId();
            this.productName = history.getProductName();
            this.productCode = history.getProductCode();
            this.quantity = history.getQuantity();
            this.amount = history.getAmount();
            this.method = history.getMethod().name();
            if (history.getOld_value() == null) {
                this.oldValue = null;
            } else {
                this.oldValue = history.getOld_value();
            }

            this.newValue = history.getNew_value();
            if (history.getReason() == null) {
                this.reason = null;
            } else {
                this.reason = history.getReason();
            }
            this.createdAt = history.getCreatedAt().toString();
            String at = this.createdAt.replace('T', ' ');
            this.createdAtText = at.length() > 16 ? at.substring(0, 16) : at;
            this.amountText = org.example.shopping._core.utils.MoneyUtils.format(this.amount);
            this.statusLabel = this.newValue == null ? "-" : switch (this.newValue) {
                case "PAID", "SUCCESS" -> "결제 완료";
                case "CONFIRMED" -> "구매 확정";
                case "REFUNDED" -> "환불";
                case "PARTIAL_REFUNDED" -> "부분 환불";
                case "FAILED" -> "실패";
                default -> this.newValue;
            };
            if (history.getUpdateAt() == null) {
                this.updatedAt = "-";
            } else {
                this.updatedAt = history.getUpdateAt().toString();
            }

        }
    }
}
