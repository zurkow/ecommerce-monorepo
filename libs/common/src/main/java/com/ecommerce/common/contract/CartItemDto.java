package com.ecommerce.common.contract;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CartItemDto(
        @NotBlank String sku,
        @NotBlank String name,
        @NotNull @DecimalMin("0.01") BigDecimal unitPrice,
        @Min(1) int quantity) {

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
