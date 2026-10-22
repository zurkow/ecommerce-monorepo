package com.ecommerce.order.client;

import com.ecommerce.common.contract.UserDto;
import com.ecommerce.common.error.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserClient {

    private final RestClient http;

    public UserClient(RestClient.Builder builder, @Value("${services.user.url}") String baseUrl) {
        this.http = builder.baseUrl(baseUrl).build();
    }

    public UserDto get(Long userId) {
        return http.get().uri("/api/users/{id}", userId)
                .retrieve()
                .onStatus(s -> s == HttpStatus.NOT_FOUND, (req, res) -> {
                    throw new NotFoundException("User " + userId + " not found");
                })
                .body(UserDto.class);
    }
}
