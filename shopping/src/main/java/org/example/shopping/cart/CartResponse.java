package org.example.shopping.cart;

import lombok.Data;
import org.example.shopping._core.utils.MoneyUtils;
import org.example.shopping.cartItem.CartItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class CartResponse {

    private static String getImagePath(String thumbnailUrl) {
        if (thumbnailUrl == null || thumbnailUrl.isEmpty()) {
            return "/img/no-image.png";
        }

        if (thumbnailUrl.startsWith("http://") || thumbnailUrl.startsWith("https://") || thumbnailUrl.startsWith("/img/")) {
            return thumbnailUrl;
        }

        return "/images/" + thumbnailUrl;
    }

    @Data
    public static class CartDTO {
        private Long id;
        private String amount;
        private List<CartItemDTO> cartItems;
        private List<SellerGroupDTO> sellerGroups; // 화면에서 판매자별로 묶어 보여 주기 위함
        private int itemCount;
        private boolean hasItems; // 머스태치는 0 을 거짓으로 보지 않아서 따로 둔다

        public CartDTO(Cart cart) {
            this.id = cart.getId();
            Long amount = cart.getAmount() == null ? 0 : cart.getAmount();
            this.amount = MoneyUtils.format(amount);
            this.cartItems = cart.getCartItems().stream()
                    .map(CartItemDTO::new)
                    .toList();
            this.itemCount = this.cartItems.size();
            this.hasItems = this.itemCount > 0;
            this.sellerGroups = this.cartItems.stream()
                    .collect(Collectors.groupingBy(CartItemDTO::getOwnerName, LinkedHashMap::new, Collectors.toList()))
                    .entrySet().stream()
                    .map(e -> new SellerGroupDTO(e.getKey(), e.getValue()))
                    .toList();
        }
    }

    @Data
    public static class SellerGroupDTO {
        private final String ownerName;
        private final List<CartItemDTO> items;
    }

    @Data
    public static class CartItemDTO {
        private Long id;
        private Long productId;
        private String ownerName;
        private Boolean isChecked;
        private String thumbnailUrl;
        private String productName;
        private String productCode;
        private Integer quantity;
        private String totalPrice;

        public CartItemDTO(CartItem cartItem) {
            this.id = cartItem.getId();
            this.productId = cartItem.getProduct().getId();
            this.ownerName = cartItem.getProduct().getOwner().getName();
            this.isChecked = cartItem.getIsChecked();
            this.thumbnailUrl = getImagePath(cartItem.getProduct().getThumbnailUrl());
            this.productName = cartItem.getProduct().getProductName();
            this.productCode = cartItem.getProduct().getProductCode();
            this.quantity = cartItem.getQuantity();
            this.totalPrice = MoneyUtils.format(cartItem.getTotalPrice());
        }
    }

    @Data
    public static class AmountDTO {
        private String amount;
        public AmountDTO(Long amount) {
            this.amount = MoneyUtils.format(amount);
        }
    }

    @Data
    public static class CartUpdateDTO {
        private Long id;
        private Integer quantity;
        private String totalPrice;
        private String amount;

        public CartUpdateDTO(Long id, Integer quantity, Long totalPrice, Long amount) {
            this.id = id;
            this.quantity = quantity;
            this.totalPrice = MoneyUtils.format(totalPrice);
            this.amount = MoneyUtils.format(amount);
        }
    }

    @Data
    public static class ToggleAllChecksDTO {
        private boolean allChecked;
        private String amount;

        public ToggleAllChecksDTO(boolean allChecked, Long amount) {
            this.allChecked = allChecked;
            this.amount = MoneyUtils.format(amount);
        }
    }
}
