package com.ecommerce.cart;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class CartControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void addingSameSkuTwiceMergesQuantityAndComputesTotal() throws Exception {
        String item = """
                {"sku":"MUG-1","name":"Mug","unitPrice":12.50,"quantity":1}
                """;
        mvc.perform(post("/api/carts/7/items").contentType(MediaType.APPLICATION_JSON).content(item));
        mvc.perform(post("/api/carts/7/items").contentType(MediaType.APPLICATION_JSON).content(item))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.total").value(25.00));

        mvc.perform(delete("/api/carts/7")).andExpect(status().isNoContent());
        mvc.perform(get("/api/carts/7")).andExpect(jsonPath("$.items.length()").value(0));
    }
}
