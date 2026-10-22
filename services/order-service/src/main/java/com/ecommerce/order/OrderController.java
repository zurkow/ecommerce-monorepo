package com.ecommerce.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    public record CheckoutRequest(@NotNull Long userId) {
    }

    public record StatusRequest(@NotNull OrderStatus status) {
    }

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<OrderDto> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public OrderDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto checkout(@Valid @RequestBody CheckoutRequest req) {
        return service.checkout(req.userId());
    }

    @PatchMapping("/{id}/status")
    public OrderDto changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest req) {
        return service.changeStatus(id, req.status());
    }
}
