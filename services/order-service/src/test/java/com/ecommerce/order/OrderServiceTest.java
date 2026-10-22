package com.ecommerce.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.common.contract.CartDto;
import com.ecommerce.common.contract.CartItemDto;
import com.ecommerce.common.contract.PaymentDto;
import com.ecommerce.common.contract.PaymentStatus;
import com.ecommerce.common.contract.UserDto;
import com.ecommerce.common.error.BusinessException;
import com.ecommerce.order.client.CartClient;
import com.ecommerce.order.client.PaymentClient;
import com.ecommerce.order.client.UserClient;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/** The other services are mocked: this test runs without anything else started. */
@SpringBootTest
class OrderServiceTest {

    @Autowired
    OrderService service;

    @MockitoBean
    UserClient users;
    @MockitoBean
    CartClient carts;
    @MockitoBean
    PaymentClient payments;

    @BeforeEach
    void setUp() {
        when(users.get(1L)).thenReturn(new UserDto(1L, "jan@shop.local", "Jan", "K", "CUSTOMER", Instant.now()));
        CartItemDto mug = new CartItemDto("MUG-1", "Mug", new BigDecimal("10.00"), 3);
        when(carts.get(1L)).thenReturn(new CartDto(1L, List.of(mug), new BigDecimal("30.00")));
    }

    @Test
    void approvedPaymentMarksOrderPaidAndClearsCart() {
        when(payments.pay(any())).thenReturn(payment(PaymentStatus.APPROVED));

        OrderDto order = service.checkout(1L);

        assertThat(order.status()).isEqualTo(OrderStatus.PAID);
        assertThat(order.total()).isEqualByComparingTo("30.00");
        assertThat(order.lines()).hasSize(1);
        verify(carts).clear(1L);
    }

    @Test
    void declinedPaymentKeepsCart() {
        when(payments.pay(any())).thenReturn(payment(PaymentStatus.DECLINED));

        OrderDto order = service.checkout(1L);

        assertThat(order.status()).isEqualTo(OrderStatus.PAYMENT_FAILED);
        verify(carts, never()).clear(any());
    }

    @Test
    void cancellingPaidOrderRefundsPayment() {
        when(payments.pay(any())).thenReturn(payment(PaymentStatus.APPROVED));
        OrderDto order = service.checkout(1L);

        assertThat(service.changeStatus(order.id(), OrderStatus.CANCELLED).status()).isEqualTo(OrderStatus.CANCELLED);
        verify(payments).refund(42L);
    }

    @Test
    void rejectsInvalidStatusTransition() {
        when(payments.pay(any())).thenReturn(payment(PaymentStatus.APPROVED));
        OrderDto order = service.checkout(1L);

        assertThatThrownBy(() -> service.changeStatus(order.id(), OrderStatus.DELIVERED))
                .isInstanceOf(BusinessException.class);
    }

    private PaymentDto payment(PaymentStatus status) {
        return new PaymentDto(42L, 1L, 1L, new BigDecimal("30.00"), status, Instant.now());
    }
}
