package com.ecommerce.order;

import com.ecommerce.common.contract.CartItemDto;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/** A snapshot of a cart item at checkout time (prices must not change afterwards). */
@Embeddable
public record OrderLine(String sku, String name, BigDecimal unitPrice, int quantity) {

    static OrderLine from(CartItemDto item) {
        return new OrderLine(item.sku(), item.name(), item.unitPrice(), item.quantity());
    }
}
