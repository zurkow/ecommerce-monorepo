package com.ecommerce.payment;

import com.ecommerce.common.contract.PaymentDto;
import com.ecommerce.common.contract.PaymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private Long userId;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private Instant createdAt;

    protected Payment() {
    }

    Payment(Long orderId, Long userId, BigDecimal amount, PaymentStatus status) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
        this.createdAt = Instant.now();
    }

    PaymentStatus getStatus() {
        return status;
    }

    void refund() {
        this.status = PaymentStatus.REFUNDED;
    }

    PaymentDto toDto() {
        return new PaymentDto(id, orderId, userId, amount, status, createdAt);
    }
}
