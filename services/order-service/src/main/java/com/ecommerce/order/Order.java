package com.ecommerce.order;

import com.ecommerce.common.contract.CartDto;
import com.ecommerce.common.error.BusinessException;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders") // "order" is a reserved SQL word
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<OrderLine> lines = new ArrayList<>();

    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Long paymentId;
    private Instant createdAt;

    protected Order() {
    }

    static Order pending(Long userId, CartDto cart) {
        Order order = new Order();
        order.userId = userId;
        order.lines = new ArrayList<>(cart.items().stream().map(OrderLine::from).toList());
        order.total = cart.total();
        order.status = OrderStatus.PENDING;
        order.createdAt = Instant.now();
        return order;
    }

    void paymentResult(Long paymentId, boolean approved) {
        this.paymentId = paymentId;
        this.status = approved ? OrderStatus.PAID : OrderStatus.PAYMENT_FAILED;
    }

    void moveTo(OrderStatus target) {
        if (!status.next().contains(target)) {
            throw new BusinessException("Cannot change order " + id + " from " + status + " to " + target
                    + " (allowed: " + status.next() + ")");
        }
        this.status = target;
    }

    Long getId() {
        return id;
    }

    Long getUserId() {
        return userId;
    }

    OrderStatus getStatus() {
        return status;
    }

    Long getPaymentId() {
        return paymentId;
    }

    OrderDto toDto() {
        return new OrderDto(id, userId, List.copyOf(lines), total, status, paymentId, createdAt);
    }
}
