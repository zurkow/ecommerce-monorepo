package com.ecommerce.order.client;

import com.ecommerce.common.contract.PaymentDto;
import com.ecommerce.common.contract.PaymentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentClient {

    private final RestClient http;

    public PaymentClient(RestClient.Builder builder, @Value("${services.payment.url}") String baseUrl) {
        this.http = builder.baseUrl(baseUrl).build();
    }

    public PaymentDto pay(PaymentRequest request) {
        return http.post().uri("/api/payments").body(request).retrieve().body(PaymentDto.class);
    }

    public PaymentDto refund(Long paymentId) {
        return http.post().uri("/api/payments/{id}/refund", paymentId).retrieve().body(PaymentDto.class);
    }
}
