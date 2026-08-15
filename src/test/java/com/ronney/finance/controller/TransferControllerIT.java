package com.ronney.finance.controller;

import com.ronney.finance.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransferControllerIT extends BaseIntegrationTest {

    private UUID createFinancialAccount(
            String token,
            String name,
            BigDecimal initialBalance
    ) throws Exception {
        String body = """
                {
                    "name": "%s",
                    "accountType": "DIGITAL_ACCOUNT",
                    "initialBalance": %s
                }
                """.formatted(
                name,
                initialBalance
        );

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
    void shouldCreateTransferAndUpdateAccountBalances()
            throws Exception {
        String token = getToken();

        UUID sourceAccountId = createFinancialAccount(
                token,
                "Source Account",
                new BigDecimal("5000.00")
        );

        UUID destinationAccountId = createFinancialAccount(
                token,
                "Destination Account",
                new BigDecimal("1000.00")
        );

        String body = """
                {
                    "description": "Transfer to savings",
                    "amount": 1500.00,
                    "transactionDate": "2026-08-15",
                    "sourceAccountId": "%s",
                    "destinationAccountId": "%s"
                }
                """.formatted(
                sourceAccountId,
                destinationAccountId
        );

        mockMvc.perform(
                        post("/api/v1/transfers")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.description")
                                .value("Transfer to savings")
                )
                .andExpect(
                        jsonPath("$.amount")
                                .value(1500.00)
                )
                .andExpect(
                        jsonPath("$.sourceAccountId")
                                .value(sourceAccountId.toString())
                )
                .andExpect(
                        jsonPath("$.destinationAccountId")
                                .value(destinationAccountId.toString())
                );

        mockMvc.perform(
                        get(
                                "/api/v1/financial-accounts/{id}",
                                sourceAccountId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.currentBalance")
                                .value(3500.00)
                );

        mockMvc.perform(
                        get(
                                "/api/v1/financial-accounts/{id}",
                                destinationAccountId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.currentBalance")
                                .value(2500.00)
                );
    }

    @Test
    void shouldNotIncludeTransferInDashboardSummary()
            throws Exception {
        String token = getToken();

        UUID sourceAccountId = createFinancialAccount(
                token,
                "Source Account",
                new BigDecimal("5000.00")
        );

        UUID destinationAccountId = createFinancialAccount(
                token,
                "Destination Account",
                new BigDecimal("1000.00")
        );

        String body = """
            {
                "description": "Transfer to savings",
                "amount": 1500.00,
                "transactionDate": "2026-08-15",
                "sourceAccountId": "%s",
                "destinationAccountId": "%s"
            }
            """.formatted(
                sourceAccountId,
                destinationAccountId
        );

        mockMvc.perform(
                        post("/api/v1/transfers")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/v1/dashboard/summary")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.totalIncome")
                                .value(0)
                )
                .andExpect(
                        jsonPath("$.totalExpense")
                                .value(0)
                )
                .andExpect(
                        jsonPath("$.balance")
                                .value(0)
                );
    }

    @Test
    void shouldNotAllowTransferBetweenSameAccount()
            throws Exception {
        String token = getToken();

        UUID accountId = createFinancialAccount(
                token,
                "Test Account",
                new BigDecimal("5000.00")
        );

        String body = """
            {
                "description": "Invalid transfer",
                "amount": 1000.00,
                "transactionDate": "2026-08-15",
                "sourceAccountId": "%s",
                "destinationAccountId": "%s"
            }
            """.formatted(
                accountId,
                accountId
        );

        mockMvc.perform(
                        post("/api/v1/transfers")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotAllowTransferUsingAnotherUsersAccount()
            throws Exception {
        String firstUserToken = getToken();

        String secondUserToken = getToken(
                "user.two@example.test",
                "test-password"
        );

        UUID firstUserAccountId = createFinancialAccount(
                firstUserToken,
                "First User Account",
                new BigDecimal("5000.00")
        );

        UUID secondUserAccountId = createFinancialAccount(
                secondUserToken,
                "Second User Account",
                new BigDecimal("1000.00")
        );

        String body = """
            {
                "description": "Unauthorized transfer",
                "amount": 500.00,
                "transactionDate": "2026-08-15",
                "sourceAccountId": "%s",
                "destinationAccountId": "%s"
            }
            """.formatted(
                firstUserAccountId,
                secondUserAccountId
        );

        mockMvc.perform(
                        post("/api/v1/transfers")
                                .header(
                                        "Authorization",
                                        "Bearer " + secondUserToken
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAllowTransferWithNonExistingAccount()
            throws Exception {
        String token = getToken();

        UUID sourceAccountId = createFinancialAccount(
                token,
                "Source Account",
                new BigDecimal("5000.00")
        );

        UUID nonExistingAccountId = UUID.randomUUID();

        String body = """
            {
                "description": "Invalid transfer",
                "amount": 500.00,
                "transactionDate": "2026-08-15",
                "sourceAccountId": "%s",
                "destinationAccountId": "%s"
            }
            """.formatted(
                sourceAccountId,
                nonExistingAccountId
        );

        mockMvc.perform(
                        post("/api/v1/transfers")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFindTransferById()
            throws Exception {
        String token = getToken();

        UUID sourceAccountId = createFinancialAccount(
                token,
                "Source Account",
                new BigDecimal("5000.00")
        );

        UUID destinationAccountId = createFinancialAccount(
                token,
                "Destination Account",
                new BigDecimal("1000.00")
        );

        String body = """
            {
                "description": "Transfer to savings",
                "amount": 1500.00,
                "transactionDate": "2026-08-15",
                "sourceAccountId": "%s",
                "destinationAccountId": "%s"
            }
            """.formatted(
                sourceAccountId,
                destinationAccountId
        );

        String response = mockMvc.perform(
                        post("/api/v1/transfers")
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

        UUID transferId = UUID.fromString(
                objectMapper
                        .readTree(response)
                        .get("id")
                        .asText()
        );

        mockMvc.perform(
                        get(
                                "/api/v1/transfers/{id}",
                                transferId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(transferId.toString())
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("Transfer to savings")
                )
                .andExpect(
                        jsonPath("$.amount")
                                .value(1500.00)
                )
                .andExpect(
                        jsonPath("$.transactionDate")
                                .value("2026-08-15")
                )
                .andExpect(
                        jsonPath("$.sourceAccountId")
                                .value(sourceAccountId.toString())
                )
                .andExpect(
                        jsonPath("$.sourceAccountName")
                                .value("Source Account")
                )
                .andExpect(
                        jsonPath("$.destinationAccountId")
                                .value(destinationAccountId.toString())
                )
                .andExpect(
                        jsonPath("$.destinationAccountName")
                                .value("Destination Account")
                );
    }

    @Test
    void shouldUpdateTransfer()
            throws Exception {
        String token = getToken();

        UUID sourceAccountId = createFinancialAccount(
                token,
                "Source Account",
                new BigDecimal("5000.00")
        );

        UUID destinationAccountId = createFinancialAccount(
                token,
                "Destination Account",
                new BigDecimal("1000.00")
        );

        String createBody = """
            {
                "description": "Initial transfer",
                "amount": 1500.00,
                "transactionDate": "2026-08-15",
                "sourceAccountId": "%s",
                "destinationAccountId": "%s"
            }
            """.formatted(
                sourceAccountId,
                destinationAccountId
        );

        String response = mockMvc.perform(
                        post("/api/v1/transfers")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(createBody)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID transferId = UUID.fromString(
                objectMapper
                        .readTree(response)
                        .get("id")
                        .asText()
        );

        String updateBody = """
            {
                "description": "Updated transfer",
                "amount": 1000.00,
                "transactionDate": "2026-08-16",
                "sourceAccountId": "%s",
                "destinationAccountId": "%s"
            }
            """.formatted(
                sourceAccountId,
                destinationAccountId
        );

        mockMvc.perform(
                        put(
                                "/api/v1/transfers/{id}",
                                transferId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(updateBody)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.description")
                                .value("Updated transfer")
                )
                .andExpect(
                        jsonPath("$.amount")
                                .value(1000.00)
                )
                .andExpect(
                        jsonPath("$.transactionDate")
                                .value("2026-08-16")
                );

        mockMvc.perform(
                        get(
                                "/api/v1/financial-accounts/{id}",
                                sourceAccountId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.currentBalance")
                                .value(4000.00)
                );

        mockMvc.perform(
                        get(
                                "/api/v1/financial-accounts/{id}",
                                destinationAccountId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.currentBalance")
                                .value(2000.00)
                );
    }

    @Test
    void shouldDeleteTransfer()
            throws Exception {
        String token = getToken();

        UUID sourceAccountId = createFinancialAccount(
                token,
                "Source Account",
                new BigDecimal("5000.00")
        );

        UUID destinationAccountId = createFinancialAccount(
                token,
                "Destination Account",
                new BigDecimal("1000.00")
        );

        String body = """
            {
                "description": "Transfer to savings",
                "amount": 1500.00,
                "transactionDate": "2026-08-15",
                "sourceAccountId": "%s",
                "destinationAccountId": "%s"
            }
            """.formatted(
                sourceAccountId,
                destinationAccountId
        );

        String response = mockMvc.perform(
                        post("/api/v1/transfers")
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

        UUID transferId = UUID.fromString(
                objectMapper
                        .readTree(response)
                        .get("id")
                        .asText()
        );

        mockMvc.perform(
                        delete(
                                "/api/v1/transfers/{id}",
                                transferId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get(
                                "/api/v1/transfers/{id}",
                                transferId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNotFound());

        mockMvc.perform(
                        get(
                                "/api/v1/financial-accounts/{id}",
                                sourceAccountId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.currentBalance")
                                .value(5000.00)
                );

        mockMvc.perform(
                        get(
                                "/api/v1/financial-accounts/{id}",
                                destinationAccountId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.currentBalance")
                                .value(1000.00)
                );
    }

    @Test
    void shouldNotAccessTransferFromAnotherUser()
            throws Exception {
        String firstUserToken = getToken();

        String secondUserToken = getToken(
                "user.two@example.test",
                "test-password"
        );

        UUID sourceAccountId = createFinancialAccount(
                firstUserToken,
                "First User Source",
                new BigDecimal("5000.00")
        );

        UUID destinationAccountId = createFinancialAccount(
                firstUserToken,
                "First User Destination",
                new BigDecimal("1000.00")
        );

        String body = """
            {
                "description": "Private transfer",
                "amount": 500.00,
                "transactionDate": "2026-08-15",
                "sourceAccountId": "%s",
                "destinationAccountId": "%s"
            }
            """.formatted(
                sourceAccountId,
                destinationAccountId
        );

        String response = mockMvc.perform(
                        post("/api/v1/transfers")
                                .header(
                                        "Authorization",
                                        "Bearer " + firstUserToken
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID transferId = UUID.fromString(
                objectMapper
                        .readTree(response)
                        .get("id")
                        .asText()
        );

        mockMvc.perform(
                        get(
                                "/api/v1/transfers/{id}",
                                transferId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + secondUserToken
                                )
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotUpdateTransferFromAnotherUser()
            throws Exception {
        String firstUserToken = getToken();

        String secondUserToken = getToken(
                "user.two@example.test",
                "test-password"
        );

        UUID sourceAccountId = createFinancialAccount(
                firstUserToken,
                "First User Source",
                new BigDecimal("5000.00")
        );

        UUID destinationAccountId = createFinancialAccount(
                firstUserToken,
                "First User Destination",
                new BigDecimal("1000.00")
        );

        String createBody = """
            {
                "description": "Private transfer",
                "amount": 500.00,
                "transactionDate": "2026-08-15",
                "sourceAccountId": "%s",
                "destinationAccountId": "%s"
            }
            """.formatted(
                sourceAccountId,
                destinationAccountId
        );

        String response = mockMvc.perform(
                        post("/api/v1/transfers")
                                .header(
                                        "Authorization",
                                        "Bearer " + firstUserToken
                                )
                                .contentType(APPLICATION_JSON)
                                .content(createBody)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID transferId = UUID.fromString(
                objectMapper
                        .readTree(response)
                        .get("id")
                        .asText()
        );

        String updateBody = """
            {
                "description": "Unauthorized update",
                "amount": 1000.00,
                "transactionDate": "2026-08-16",
                "sourceAccountId": "%s",
                "destinationAccountId": "%s"
            }
            """.formatted(
                sourceAccountId,
                destinationAccountId
        );

        mockMvc.perform(
                        put(
                                "/api/v1/transfers/{id}",
                                transferId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + secondUserToken
                                )
                                .contentType(APPLICATION_JSON)
                                .content(updateBody)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotDeleteTransferFromAnotherUser()
            throws Exception {
        String firstUserToken = getToken();

        String secondUserToken = getToken(
                "user.two@example.test",
                "test-password"
        );

        UUID sourceAccountId = createFinancialAccount(
                firstUserToken,
                "First User Source",
                new BigDecimal("5000.00")
        );

        UUID destinationAccountId = createFinancialAccount(
                firstUserToken,
                "First User Destination",
                new BigDecimal("1000.00")
        );

        String body = """
            {
                "description": "Private transfer",
                "amount": 500.00,
                "transactionDate": "2026-08-15",
                "sourceAccountId": "%s",
                "destinationAccountId": "%s"
            }
            """.formatted(
                sourceAccountId,
                destinationAccountId
        );

        String response = mockMvc.perform(
                        post("/api/v1/transfers")
                                .header(
                                        "Authorization",
                                        "Bearer " + firstUserToken
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID transferId = UUID.fromString(
                objectMapper
                        .readTree(response)
                        .get("id")
                        .asText()
        );

        mockMvc.perform(
                        delete(
                                "/api/v1/transfers/{id}",
                                transferId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + secondUserToken
                                )
                )
                .andExpect(status().isNotFound());

        mockMvc.perform(
                        get(
                                "/api/v1/transfers/{id}",
                                transferId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + firstUserToken
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateTransferAccounts()
            throws Exception {
        String token = getToken();

        UUID originalSourceAccountId = createFinancialAccount(
                token,
                "Original Source",
                new BigDecimal("5000.00")
        );

        UUID originalDestinationAccountId = createFinancialAccount(
                token,
                "Original Destination",
                new BigDecimal("1000.00")
        );

        UUID newSourceAccountId = createFinancialAccount(
                token,
                "New Source",
                new BigDecimal("3000.00")
        );

        UUID newDestinationAccountId = createFinancialAccount(
                token,
                "New Destination",
                new BigDecimal("2000.00")
        );

        String createBody = """
            {
                "description": "Initial transfer",
                "amount": 1000.00,
                "transactionDate": "2026-08-15",
                "sourceAccountId": "%s",
                "destinationAccountId": "%s"
            }
            """.formatted(
                originalSourceAccountId,
                originalDestinationAccountId
        );

        String response = mockMvc.perform(
                        post("/api/v1/transfers")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(createBody)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID transferId = UUID.fromString(
                objectMapper
                        .readTree(response)
                        .get("id")
                        .asText()
        );

        String updateBody = """
            {
                "description": "Updated transfer",
                "amount": 1500.00,
                "transactionDate": "2026-08-16",
                "sourceAccountId": "%s",
                "destinationAccountId": "%s"
            }
            """.formatted(
                newSourceAccountId,
                newDestinationAccountId
        );

        mockMvc.perform(
                        put(
                                "/api/v1/transfers/{id}",
                                transferId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(updateBody)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get(
                                "/api/v1/financial-accounts/{id}",
                                originalSourceAccountId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.currentBalance")
                                .value(5000.00)
                );

        mockMvc.perform(
                        get(
                                "/api/v1/financial-accounts/{id}",
                                originalDestinationAccountId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.currentBalance")
                                .value(1000.00)
                );

        mockMvc.perform(
                        get(
                                "/api/v1/financial-accounts/{id}",
                                newSourceAccountId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.currentBalance")
                                .value(1500.00)
                );

        mockMvc.perform(
                        get(
                                "/api/v1/financial-accounts/{id}",
                                newDestinationAccountId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.currentBalance")
                                .value(3500.00)
                );
    }
}