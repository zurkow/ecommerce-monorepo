package com.ecommerce.payment;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void approvesSmallPaymentAndDeclinesLargeOne() throws Exception {
        mvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":1,\"userId\":1,\"amount\":99.99}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":2,\"userId\":1,\"amount\":5000}"))
                .andExpect(jsonPath("$.status").value("DECLINED"));
    }

    @Test
    void cannotRefundDeclinedPayment() throws Exception {
        String id = mvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":3,\"userId\":1,\"amount\":5000}"))
                .andReturn().getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(post("/api/payments/" + id + "/refund")).andExpect(status().isConflict());
    }
}
