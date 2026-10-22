package com.ecommerce.cart;

import com.ecommerce.common.contract.CartItemDto;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class CartItem {

    private String sku;
    private String name;
    private BigDecimal unitPrice;
    private int quantity;

    protected CartItem() {
    }

    CartItem(CartItemDto dto) {
        this.sku = dto.sku();
        this.name = dto.name();
        this.unitPrice = dto.unitPrice();
        this.quantity = dto.quantity();
    }

    void increase(int by) {
        this.quantity += by;
    }

    String getSku() {
        return sku;
    }

    CartItemDto toDto() {
        return new CartItemDto(sku, name, unitPrice, quantity);
    }
}
