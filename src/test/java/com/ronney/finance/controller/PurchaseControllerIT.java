package com.ronney.finance.controller;

import com.ronney.finance.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PurchaseControllerIT extends BaseIntegrationTest {

    private UUID createCreditCard(String token) throws Exception {
        String body = """
                {
                    "name":"Nubank",
                    "creditLimit":10000,
                    "closingDay":20,
                    "dueDay":28
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

        return UUID.fromString(objectMapper
                .readTree(response)
                .get("id")
                .asText()
        );
    }

    private UUID createFinancialAccount(
            String token
    ) throws Exception {

        String body = """
            {
                "name": "Nubank Account",
                "accountType": "DIGITAL_ACCOUNT",
                "initialBalance": 5000.00
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

    private void payInvoice(
            String token,
            UUID cardId,
            UUID accountId
    ) throws Exception {

        String body = """
            {
                "accountId": "%s",
                "paymentMethod": "PIX"
            }
            """.formatted(accountId);

        mockMvc.perform(
                        post(
                                "/api/v1/credit-cards/{id}/invoice/pay",
                                cardId
                        )
                                .param("month", "10")
                                .param("year", "2026")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNoContent());
    }

    private void createPurchase(
            String token,
            UUID cardId
    ) throws Exception {
        String body = """
        {
            "description":"Notebook Dell",
            "totalAmount":12000,
            "installments":12,
            "purchaseDate":"2026-09-25"
        }
        """;

        mockMvc.perform(
                post("/api/v1/credit-cards/{id}/purchases", cardId)
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(APPLICATION_JSON)
                        .content(body)
                )
                .andExpect(status().isCreated());
    }

    @Test
    void shouldCreatePurchaseWithCorrectInstallmentDistribution() throws Exception {

        String token = getToken();
        UUID cardId = createCreditCard(token);

        String body = """
            {
                "description":"Compra teste aggregate",
                "totalAmount":100.00,
                "installments":3,
                "purchaseDate":"2026-07-08"
            }
            """;

        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/purchases", cardId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].description")
                        .value("Compra teste aggregate"))
                .andExpect(jsonPath("$[0].installmentNumber").value(1))
                .andExpect(jsonPath("$[0].totalInstallments").value(3))
                .andExpect(jsonPath("$[0].amount").value(33.33))
                .andExpect(jsonPath("$[1].amount").value(33.33))
                .andExpect(jsonPath("$[2].amount").value(33.34));
    }

    @Test
    void shouldGetInvoiceWithPurchaseData() throws Exception {

        String token = getToken();
        UUID cardId = createCreditCard(token);

        createPurchase(token, cardId);

        mockMvc.perform(
                        get("/api/v1/credit-cards/{id}/invoice", cardId)
                                .param("month", "10")
                                .param("year", "2026")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.card").value("Nubank"))
                .andExpect(jsonPath("$.month").value(10))
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.total").value(1000))
                .andExpect(jsonPath("$.availableLimit").value(-2000))
                .andExpect(jsonPath("$.installments.length()").value(1))
                .andExpect(jsonPath("$.installments[0].description")
                        .value("Notebook Dell"))
                .andExpect(jsonPath("$.installments[0].installment")
                        .value("1/12"))
                .andExpect(jsonPath("$.installments[0].amount")
                        .value(1000))
                .andExpect(jsonPath("$.installments[0].paid")
                        .value(false))
                .andExpect(jsonPath("$.installments[0].paidAt")
                        .doesNotExist());
    }

    @Test
    void shouldPayInvoiceAndRestoreAvailableLimit() throws Exception {

        String token = getToken();

        UUID cardId = createCreditCard(token);
        UUID accountId = createFinancialAccount(token);

        createPurchase(token, cardId);

        payInvoice(
                token,
                cardId,
                accountId
        );

        mockMvc.perform(
                        get("/api/v1/credit-cards/{id}/invoice", cardId)
                                .param("month", "10")
                                .param("year", "2026")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1000))
                .andExpect(jsonPath("$.availableLimit").value(-1000))
                .andExpect(jsonPath("$.installments.length()").value(1))
                .andExpect(jsonPath("$.installments[0].description")
                        .value("Notebook Dell"))
                .andExpect(jsonPath("$.installments[0].installment")
                        .value("1/12"))
                .andExpect(jsonPath("$.installments[0].amount")
                        .value(1000))
                .andExpect(jsonPath("$.installments[0].paid")
                        .value(true))
                .andExpect(jsonPath("$.installments[0].paidAt")
                        .isNotEmpty());
    }

    @Test
    void shouldNotPayInvoiceTwice() throws Exception {

        String token = getToken();

        UUID cardId = createCreditCard(token);
        UUID accountId = createFinancialAccount(token);

        createPurchase(token, cardId);

        payInvoice(
                token,
                cardId,
                accountId
        );

        String body = """
            {
                "accountId": "%s",
                "paymentMethod": "PIX"
            }
            """.formatted(accountId);

        mockMvc.perform(
                        post(
                                "/api/v1/credit-cards/{id}/invoice/pay",
                                cardId
                        )
                                .param("month", "10")
                                .param("year", "2026")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isConflict());
    }

    @Test
    void shouldCreateTransactionWhenPayingInvoice() throws Exception {

        String token = getToken();

        UUID cardId = createCreditCard(token);
        UUID accountId = createFinancialAccount(token);

        createPurchase(token, cardId);

        payInvoice(
                token,
                cardId,
                accountId
        );

        mockMvc.perform(
                        get("/api/v1/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.content[?(@.transactionKind == 'CREDIT_CARD_PAYMENT' && @.accountId == '%s')].description"
                                        .formatted(accountId)
                        )
                                .value(
                                        hasItem(
                                                "Credit card invoice payment - Nubank 10/2026"
                                        )
                                )
                )
                .andExpect(
                        jsonPath(
                                "$.content[?(@.transactionKind == 'CREDIT_CARD_PAYMENT' && @.accountId == '%s')].amount"
                                        .formatted(accountId)
                        )
                                .value(
                                        hasItem(1000.0)
                                )
                )
                .andExpect(
                        jsonPath(
                                "$.content[?(@.transactionKind == 'CREDIT_CARD_PAYMENT' && @.accountId == '%s')].type"
                                        .formatted(accountId)
                        )
                                .value(
                                        hasItem("EXPENSE")
                                )
                )
                .andExpect(
                        jsonPath(
                                "$.content[?(@.transactionKind == 'CREDIT_CARD_PAYMENT' && @.accountId == '%s')].paymentMethod"
                                        .formatted(accountId)
                        )
                                .value(
                                        hasItem("PIX")
                                )
                )
                .andExpect(
                        jsonPath(
                                "$.content[?(@.transactionKind == 'CREDIT_CARD_PAYMENT' && @.accountId == '%s')].accountName"
                                        .formatted(accountId)
                        )
                                .value(
                                        hasItem("Nubank Account")
                                )
                );
    }

    @Test
    void shouldNotPayInvoiceUsingAnotherUsersFinancialAccount()
            throws Exception {

        String userOneToken = getToken();

        String userTwoToken = getToken(
                "user.two@example.test",
                "test-password"
        );

        UUID cardId = createCreditCard(userOneToken);

        UUID userTwoAccountId =
                createFinancialAccount(userTwoToken);

        createPurchase(
                userOneToken,
                cardId
        );

        String body = """
            {
                "accountId": "%s",
                "paymentMethod": "PIX"
            }
            """.formatted(userTwoAccountId);

        mockMvc.perform(
                        post(
                                "/api/v1/credit-cards/{id}/invoice/pay",
                                cardId
                        )
                                .param("month", "10")
                                .param("year", "2026")
                                .header(
                                        "Authorization",
                                        "Bearer " + userOneToken
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound());

        mockMvc.perform(
                        get(
                                "/api/v1/credit-cards/{id}/invoice",
                                cardId
                        )
                                .param("month", "10")
                                .param("year", "2026")
                                .header(
                                        "Authorization",
                                        "Bearer " + userOneToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.installments[0].paid")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.installments[0].paidAt")
                                .doesNotExist()
                );
    }

    @Test
    void shouldReduceFinancialAccountBalanceWhenPayingInvoice()
            throws Exception {

        String token = getToken();

        UUID cardId = createCreditCard(token);
        UUID accountId = createFinancialAccount(token);

        createPurchase(
                token,
                cardId
        );

        payInvoice(
                token,
                cardId,
                accountId
        );

        mockMvc.perform(
                        get(
                                "/api/v1/financial-accounts/{id}",
                                accountId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.initialBalance")
                                .value(5000.00)
                )
                .andExpect(
                        jsonPath("$.currentBalance")
                                .value(4000.00)
                );
    }
}
