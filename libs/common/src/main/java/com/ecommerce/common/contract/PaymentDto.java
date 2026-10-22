package com.ecommerce.common.contract;

import java.math.BigDecimal;
import java.time.Instant;

/** Returned by payment-service, consumed by order-service. */
public record PaymentDto(Long id, Long orderId, Long userId, BigDecimal amount, PaymentStatus status, Instant createdAt) {
}
