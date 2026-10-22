package com.ecommerce.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Lives in order-service, NOT in libs/common: no other service consumes it.
 * Rule of thumb - only share what is really shared.
 */
public record OrderDto(Long id, Long userId, List<OrderLine> lines, BigDecimal total, OrderStatus status,
                       Long paymentId, Instant createdAt) {
}
