package com.ecommerce.common.error;

/** Thrown when a request is valid but breaks a business rule. Mapped to HTTP 409. */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
