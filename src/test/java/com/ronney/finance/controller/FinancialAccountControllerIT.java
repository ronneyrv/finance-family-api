package com.ronney.finance.controller;

import com.ronney.finance.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FinancialAccountControllerIT extends BaseIntegrationTest {

    private UUID createFinancialAccount(
            String token
    ) throws Exception {

        String body = """
                {
                    "name": "Test Account",
                    "accountType": "DIGITAL_ACCOUNT",
                    "initialBalance": 2500.00
                }
                """;

        String response = mockMvc.perform(
                        post("/api/v1/financial-accounts")
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
    void shouldCreateFinancialAccount()
            throws Exception {

        String token = getToken();

        UUID id = createFinancialAccount(token);

        assertNotNull(id);
    }

    @Test
    void shouldListFinancialAccounts()
            throws Exception {

        String token = getToken();

        createFinancialAccount(token);

        mockMvc.perform(
                        get("/api/v1/financial-accounts")
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
    void shouldFindFinancialAccountById()
            throws Exception {

        String token = getToken();

        UUID id = createFinancialAccount(token);

        mockMvc.perform(
                        get(
                                "/api/v1/financial-accounts/{id}",
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
                                .value("Test Account")
                )
                .andExpect(
                        jsonPath("$.accountType")
                                .value("DIGITAL_ACCOUNT")
                )
                .andExpect(
                        jsonPath("$.initialBalance")
                                .value(2500.00)
                );
    }

    @Test
    void shouldUpdateFinancialAccount()
            throws Exception {

        String token = getToken();

        UUID id = createFinancialAccount(token);

        String body = """
                {
                    "name": "Updated Account",
                    "accountType": "CHECKING_ACCOUNT",
                    "initialBalance": 3500.00
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/v1/financial-accounts/{id}",
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
                                .value("Updated Account")
                )
                .andExpect(
                        jsonPath("$.accountType")
                                .value("CHECKING_ACCOUNT")
                )
                .andExpect(
                        jsonPath("$.initialBalance")
                                .value(3500.00)
                );
    }

    @Test
    void shouldDeleteFinancialAccount()
            throws Exception {

        String token = getToken();

        UUID id = createFinancialAccount(token);

        mockMvc.perform(
                        delete(
                                "/api/v1/financial-accounts/{id}",
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
                                "/api/v1/financial-accounts/{id}",
                                id
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAccessFinancialAccountFromAnotherUser()
            throws Exception {

        String firstUserToken = getToken();

        String secondUserToken = getToken(
                "user.two@example.test",
                "test-password"
        );

        UUID accountId =
                createFinancialAccount(firstUserToken);

        mockMvc.perform(
                        get(
                                "/api/v1/financial-accounts/{id}",
                                accountId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + secondUserToken
                                )
                )
                .andExpect(status().isNotFound());
    }
}