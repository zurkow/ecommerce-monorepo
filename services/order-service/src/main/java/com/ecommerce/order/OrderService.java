package com.ecommerce.order;

import com.ecommerce.common.contract.CartDto;
import com.ecommerce.common.contract.PaymentDto;
import com.ecommerce.common.contract.PaymentRequest;
import com.ecommerce.common.contract.PaymentStatus;
import com.ecommerce.common.error.BusinessException;
import com.ecommerce.common.error.NotFoundException;
import com.ecommerce.order.client.CartClient;
import com.ecommerce.order.client.PaymentClient;
import com.ecommerce.order.client.UserClient;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orders;
    private final UserClient users;
    private final CartClient carts;
    private final PaymentClient payments;

    public OrderService(OrderRepository orders, UserClient users, CartClient carts, PaymentClient payments) {
        this.orders = orders;
        this.users = users;
        this.carts = carts;
        this.payments = payments;
    }

    /**
     * Checkout flow (synchronous REST orchestration):
     * user-service (user exists?) -> cart-service (what to buy) -> save PENDING order
     * -> payment-service (charge) -> PAID / PAYMENT_FAILED -> cart-service (clear on success)
     */
    public OrderDto checkout(Long userId) {
        users.get(userId);

        CartDto cart = carts.get(userId);
        if (cart.items().isEmpty()) {
            throw new BusinessException("Cart of user " + userId + " is empty");
        }

        Order order = orders.save(Order.pending(userId, cart));

        PaymentDto payment = payments.pay(new PaymentRequest(order.getId(), userId, cart.total()));
        boolean approved = payment.status() == PaymentStatus.APPROVED;
        order.paymentResult(payment.id(), approved);
        order = orders.save(order);

        if (approved) {
            carts.clear(userId);
        }
        return order.toDto();
    }

    public OrderDto changeStatus(Long orderId, OrderStatus target) {
        Order order = find(orderId);
        order.moveTo(target);
        if (target == OrderStatus.CANCELLED) {
            payments.refund(order.getPaymentId());
        }
        return orders.save(order).toDto();
    }

    public List<OrderDto> list() {
        return orders.findAll().stream().map(Order::toDto).toList();
    }

    public OrderDto get(Long id) {
        return find(id).toDto();
    }

    private Order find(Long id) {
        return orders.findById(id).orElseThrow(() -> new NotFoundException("Order " + id + " not found"));
    }
}
