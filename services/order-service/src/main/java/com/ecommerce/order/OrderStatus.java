package com.ecommerce.order;

import java.util.Set;

public enum OrderStatus {
    PENDING,
    PAYMENT_FAILED,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    /** Which statuses an admin may move an order to from this one. */
    public Set<OrderStatus> next() {
        return switch (this) {
            case PAID -> Set.of(SHIPPED, CANCELLED);
            case SHIPPED -> Set.of(DELIVERED);
            default -> Set.of();
        };
    }
}
