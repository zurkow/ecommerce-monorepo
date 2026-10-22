package com.ecommerce.cart;

import com.ecommerce.common.contract.CartDto;
import com.ecommerce.common.contract.CartItemDto;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** One cart per user, so the user id is the primary key. */
@Entity
public class Cart {

    @Id
    private Long userId;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<CartItem> items = new ArrayList<>();

    protected Cart() {
    }

    Cart(Long userId) {
        this.userId = userId;
    }

    void add(CartItemDto dto) {
        items.stream()
                .filter(i -> i.getSku().equals(dto.sku()))
                .findFirst()
                .ifPresentOrElse(i -> i.increase(dto.quantity()), () -> items.add(new CartItem(dto)));
    }

    void remove(String sku) {
        items.removeIf(i -> i.getSku().equals(sku));
    }

    boolean isEmpty() {
        return items.isEmpty();
    }

    CartDto toDto() {
        List<CartItemDto> dtos = items.stream().map(CartItem::toDto).toList();
        BigDecimal total = dtos.stream().map(CartItemDto::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartDto(userId, dtos, total);
    }
}
