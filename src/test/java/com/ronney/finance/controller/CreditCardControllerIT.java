package com.ronney.finance.controller;

import com.ronney.finance.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CreditCardControllerIT extends BaseIntegrationTest {

    private UUID createCreditCard(
            String token
    ) throws Exception {

        String body = """
                {
                    "name": "Test Card",
                    "creditLimit": 10000.00,
                    "closingDay": 20,
                    "dueDay": 28
                }
                """;

        String response = mockMvc.perform(
                        post("/api/v1/credit-cards")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(
                objectMapper
                        .readTree(response)
                        .get("id")
                        .asText()
        );
    }

    @Test
    void shouldCreateCreditCard()
            throws Exception {

        String token = getToken();

        UUID id = createCreditCard(token);

        assertNotNull(id);
    }

    @Test
    void shouldListCreditCards()
            throws Exception {

        String token = getToken();

        createCreditCard(token);

        mockMvc.perform(
                        get("/api/v1/credit-cards")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void shouldFindCreditCardById()
            throws Exception {

        String token = getToken();

        UUID id = createCreditCard(token);

        mockMvc.perform(
                        get(
                                "/api/v1/credit-cards/{id}",
                                id
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(id.toString())
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Test Card")
                );
    }

    @Test
    void shouldUpdateCreditCard()
            throws Exception {

        String token = getToken();

        UUID id = createCreditCard(token);

        String body = """
                {
                    "name": "Updated Test Card",
                    "creditLimit": 15000.00,
                    "closingDay": 15,
                    "dueDay": 25
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/v1/credit-cards/{id}",
                                id
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.name")
                                .value("Updated Test Card")
                )
                .andExpect(
                        jsonPath("$.creditLimit")
                                .value(15000.00)
                )
                .andExpect(
                        jsonPath("$.closingDay")
                                .value(15)
                )
                .andExpect(
                        jsonPath("$.dueDay")
                                .value(25)
                );
    }

    @Test
    void shouldDeleteCreditCard()
            throws Exception {

        String token = getToken();

        UUID id = createCreditCard(token);

        mockMvc.perform(
                        delete(
                                "/api/v1/credit-cards/{id}",
                                id
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get(
                                "/api/v1/credit-cards/{id}",
                                id
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNotFound());
    }
}
