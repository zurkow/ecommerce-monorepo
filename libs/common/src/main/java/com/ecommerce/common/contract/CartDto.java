package com.ecommerce.common.contract;

import java.math.BigDecimal;
import java.util.List;

/** Returned by cart-service, consumed by order-service. */
public record CartDto(Long userId, List<CartItemDto> items, BigDecimal total) {
}
