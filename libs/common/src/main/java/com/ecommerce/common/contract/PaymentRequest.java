package com.ecommerce.common.contract;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Sent by order-service to payment-service. */
public record PaymentRequest(
        @NotNull Long orderId,
        @NotNull Long userId,
        @NotNull @DecimalMin("0.01") BigDecimal amount) {
}
