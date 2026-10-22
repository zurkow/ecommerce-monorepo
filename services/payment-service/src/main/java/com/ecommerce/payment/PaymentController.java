package com.ecommerce.payment;

import com.ecommerce.common.contract.PaymentDto;
import com.ecommerce.common.contract.PaymentRequest;
import com.ecommerce.common.contract.PaymentStatus;
import com.ecommerce.common.error.BusinessException;
import com.ecommerce.common.error.NotFoundException;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Fake payment gateway: approves every payment up to {@code payment.limit},
 * declines anything above it. Handy for demoing the failure path in order-service.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentRepository payments;
    private final BigDecimal limit;

    public PaymentController(PaymentRepository payments, @Value("${payment.limit:1000}") BigDecimal limit) {
        this.payments = payments;
        this.limit = limit;
    }

    @GetMapping
    public List<PaymentDto> list() {
        return payments.findAll().stream().map(Payment::toDto).toList();
    }

    @GetMapping("/{id}")
    public PaymentDto get(@PathVariable Long id) {
        return find(id).toDto();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentDto pay(@Valid @RequestBody PaymentRequest req) {
        PaymentStatus status = req.amount().compareTo(limit) <= 0 ? PaymentStatus.APPROVED : PaymentStatus.DECLINED;
        return payments.save(new Payment(req.orderId(), req.userId(), req.amount(), status)).toDto();
    }

    @PostMapping("/{id}/refund")
    @Transactional
    public PaymentDto refund(@PathVariable Long id) {
        Payment payment = find(id);
        if (payment.getStatus() != PaymentStatus.APPROVED) {
            throw new BusinessException("Only APPROVED payments can be refunded, this one is " + payment.getStatus());
        }
        payment.refund();
        return payment.toDto();
    }

    private Payment find(Long id) {
        return payments.findById(id).orElseThrow(() -> new NotFoundException("Payment " + id + " not found"));
    }
}
