package com.ecommerce.order.client;

import com.ecommerce.common.contract.CartDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CartClient {

    private final RestClient http;

    public CartClient(RestClient.Builder builder, @Value("${services.cart.url}") String baseUrl) {
        this.http = builder.baseUrl(baseUrl).build();
    }

    public CartDto get(Long userId) {
        return http.get().uri("/api/carts/{userId}", userId).retrieve().body(CartDto.class);
    }

    public void clear(Long userId) {
        http.delete().uri("/api/carts/{userId}", userId).retrieve().toBodilessEntity();
    }
}
