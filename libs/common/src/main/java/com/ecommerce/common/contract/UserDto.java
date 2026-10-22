package com.ecommerce.common.contract;

import java.time.Instant;

/** Returned by user-service, consumed by order-service. */
public record UserDto(Long id, String email, String firstName, String lastName, String role, Instant createdAt) {
}
