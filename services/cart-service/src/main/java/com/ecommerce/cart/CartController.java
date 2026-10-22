package com.ecommerce.cart;

import com.ecommerce.common.contract.CartDto;
import com.ecommerce.common.contract.CartItemDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Note: cart-service trusts the userId it receives and does not call user-service.
 * Only order-service validates the user at checkout time.
 */
@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartRepository carts;

    public CartController(CartRepository carts) {
        this.carts = carts;
    }

    /** Admin view: all non-empty carts. */
    @GetMapping
    public List<CartDto> list() {
        return carts.findAll().stream().filter(c -> !c.isEmpty()).map(Cart::toDto).toList();
    }

    /** A user without a cart simply has an empty one. */
    @GetMapping("/{userId}")
    public CartDto get(@PathVariable Long userId) {
        return carts.findById(userId).orElseGet(() -> new Cart(userId)).toDto();
    }

    @PostMapping("/{userId}/items")
    @Transactional
    public CartDto addItem(@PathVariable Long userId, @Valid @RequestBody CartItemDto item) {
        Cart cart = carts.findById(userId).orElseGet(() -> carts.save(new Cart(userId)));
        cart.add(item);
        return cart.toDto();
    }

    @DeleteMapping("/{userId}/items/{sku}")
    @Transactional
    public CartDto removeItem(@PathVariable Long userId, @PathVariable String sku) {
        Cart cart = carts.findById(userId).orElseGet(() -> new Cart(userId));
        cart.remove(sku);
        return cart.toDto();
    }

    /** Called by order-service after a successful checkout. */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@PathVariable Long userId) {
        carts.deleteById(userId);
    }
}
