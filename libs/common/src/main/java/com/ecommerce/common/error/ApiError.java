package com.ecommerce.common.error;

import java.time.Instant;
import java.util.List;

/** The single error format returned by every service in the monorepo. */
public record ApiError(Instant timestamp, int status, String error, String message, List<String> details) {

    public static ApiError of(int status, String error, String message) {
        return new ApiError(Instant.now(), status, error, message, List.of());
    }
}
