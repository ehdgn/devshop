package org.example.shopping.order;

import lombok.Data;
import org.example.shopping._core.utils.MoneyUtils;
import org.example.shopping._core.utils.MyDateUtil;
import org.example.shopping.orderItem.OrderItem;
import org.example.shopping.orderItem.OrderItemStatus;
import org.example.shopping.payment.Payment;
import org.example.shopping.payment.paymentEnum.PaymentMethod;

import java.util.List;

public class OrderResponse {

    private static String getImagePath(String thumbnailUrl) {
        if (thumbnailUrl == null || thumbnailUrl.isEmpty()) {
            return "/img/no-image.png";
        }

        if (thumbnailUrl.startsWith("http://") || thumbnailUrl.startsWith("https://")) {
            return thumbnailUrl;
        }

        return "/images/" + thumbnailUrl;
    }

    @Data
    public static class OrderListDTO {
        private Long id;
        private String orderNumber;
        private List<OrderItemViewDTO> orderItems;
        private String orderedAt;

        public OrderListDTO(Order order) {
            this.id = order.getId();
            this.orderNumber = order.getOrderNumber();
            this.orderItems = order.getOrderItems().stream().map(OrderItemViewDTO::new).toList();
            this.orderedAt = MyDateUtil.toDateString(order.getCreatedAt());
        }
    }

    @Data
    public static class OrderItemViewDTO {
        private Long id;
        private String orderAmount;
        private String productName;
        private String productCode;
        private Integer quantity;
        private String statusDisplay;
        private OrderItemStatus status;
        private String thumbnailUrl;
        private Long productId;

        public OrderItemViewDTO(OrderItem item) {
            this.id = item.getId();
            this.orderAmount = MoneyUtils.decimalFormat.format(item.getAmount());
            this.productName = item.getProductName();
            this.productCode = item.getProductCode();
            this.quantity = item.getQuantity();
            this.thumbnailUrl = getImagePath(item.getProduct().getThumbnailUrl());
            this.status = item.getStatus();
            this.statusDisplay = switch (item.getStatus()) {
                case PAID -> "구매완료";
                case CONFIRMED -> "구매확정";
                case REFUNDED -> "환불";
            };
            this.productId = item.getProduct().getId();
        }
    }

    @Data
    public static class OrderDetailDTO {
        private Long id;
        private List<OrderItemViewDTO> orderItems;
        private String totalAmount;
        private PaymentMethod method;
        private String approvedAt;

        public OrderDetailDTO(Order order) {
            this.id = order.getId();
            this.orderItems = order.getOrderItems().stream().map(OrderItemViewDTO::new).toList();

            Payment payment = order.getPayment();
            // 환불된 상품을 뺀 실제 결제 금액
            this.totalAmount = MoneyUtils.decimalFormat.format(payment.getRemainingAmount());
            this.method = payment.getMethod();
            this.approvedAt = MyDateUtil.toDateString(payment.getApprovedAt());
        }
    }
}
