package com.ronney.finance.controller;

import com.ronney.finance.BaseIntegrationTest;
import com.ronney.finance.domain.entity.Category;
import com.ronney.finance.domain.entity.SubCategory;
import com.ronney.finance.domain.enums.TransactionType;
import com.ronney.finance.repository.CategoryRepository;
import com.ronney.finance.repository.SubCategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionControllerIT extends BaseIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    private UUID createFinancialAccount(
            String token
    ) throws Exception {

        String body = """
            {
                "name": "Transaction Test Account",
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

    private UUID createTransaction(String token) throws Exception {

        Category category = categoryRepository
                        .findByName("Receita")
                        .orElseThrow();

        SubCategory subCategory = subCategoryRepository
                        .findByName("Salário")
                        .orElseThrow();

        UUID accountId = createFinancialAccount(token);

        String body = """
                {
                    "description":"Salário Junho",
                    "amount":10000,
                    "transactionDate":"2026-06-23",
                    "type":"INCOME",
                    "paymentMethod":"BANK_TRANSFER",
                    "accountId":"%s",
                    "categoryId":"%s",
                    "subCategoryId":"%s"
                }
                """.formatted(
                accountId,
                category.getId(),
                subCategory.getId()
        );

        String response = mockMvc.perform(
                                post("/api/v1/transactions")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .contentType(
                                                APPLICATION_JSON
                                        )
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

    private UUID createTransactionWithDate(
            String token,
            String transactionDate
    ) throws Exception {
        Category category = categoryRepository
                .findByName("Receita")
                .orElseThrow();

        SubCategory subCategory = subCategoryRepository
                .findByName("Salário")
                .orElseThrow();

        UUID accountId = createFinancialAccount(token);

        String body = """
            {
                "description":"Salário Junho",
                "amount":10000,
                "transactionDate":"%s",
                "type":"INCOME",
                "paymentMethod":"BANK_TRANSFER",
                "accountId":"%s",
                "categoryId":"%s",
                "subCategoryId":"%s"
            }
            """.formatted(
                transactionDate,
                accountId,
                category.getId(),
                subCategory.getId()
        );

        String response = mockMvc.perform(
                        post("/api/v1/transactions")
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

    private Category createExpenseCategory() {

        Category category = Category.builder()
                .id(UUID.randomUUID())
                .name("Expense Test " + UUID.randomUUID())
                .type(TransactionType.EXPENSE)
                .build();

        return categoryRepository.save(category);
    }

    @Test
    void shouldCreateTransaction() throws Exception {

        String token = getToken();

        UUID id = createTransaction(token);

        assertNotNull(id);
    }

    @Test
    void shouldListTransactions() throws Exception {

        String token = getToken();

        createTransaction(token);

        mockMvc.perform(
                        get("/api/v1/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldFilterTransactionsByDateRange() throws Exception {
        String token = getToken();

        createTransactionWithDate(token, "2026-06-10");
        createTransactionWithDate(token, "2026-06-23");
        createTransactionWithDate(token, "2026-07-05");

        mockMvc.perform(
                        get("/api/v1/transactions")
                                .param("startDate", "2026-06-01")
                                .param("endDate", "2026-06-30")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(
                        jsonPath("$.content[0].transactionDate")
                                .value("2026-06-23")
                )
                .andExpect(
                        jsonPath("$.content[1].transactionDate")
                                .value("2026-06-10")
                );
    }

    @Test
    void shouldFilterTransactionsByCategory() throws Exception {
        String token = getToken();

        Category category = createExpenseCategory();

        UUID accountId = createFinancialAccount(token);

        String body = """
            {
                "description":"Despesa filtrada",
                "amount":150.00,
                "transactionDate":"2026-06-23",
                "type":"EXPENSE",
                "paymentMethod":"PIX",
                "accountId":"%s",
                "categoryId":"%s",
                "subCategoryId":null
            }
            """.formatted(
                accountId,
                category.getId()
        );

        mockMvc.perform(
                        post("/api/v1/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/v1/transactions")
                                .param(
                                        "categoryId",
                                        category.getId().toString()
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(
                        jsonPath("$.content[0].categoryId")
                                .value(category.getId().toString())
                )
                .andExpect(
                        jsonPath("$.content[0].description")
                                .value("Despesa filtrada")
                );
    }

    @Test
    void shouldFilterTransactionsByDescription() throws Exception {
        String token = getToken();

        createTransactionWithDate(token, "2026-06-10");

        mockMvc.perform(
                        get("/api/v1/transactions")
                                .param("description", "salário")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(
                        jsonPath("$.content[0].description")
                                .value("Salário Junho")
                );
    }

    @Test
    void shouldFilterTransactionsByDateRangeAndDescription() throws Exception {
        String token = getToken();

        createTransactionWithDate(token, "2026-06-10");
        createTransactionWithDate(token, "2026-07-10");

        mockMvc.perform(
                        get("/api/v1/transactions")
                                .param("startDate", "2026-06-01")
                                .param("endDate", "2026-06-30")
                                .param("description", "salário")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(
                        jsonPath("$.content[0].transactionDate")
                                .value("2026-06-10")
                )
                .andExpect(
                        jsonPath("$.content[0].description")
                                .value("Salário Junho")
                );
    }

    @Test
    void shouldFindTransactionById() throws Exception {

        String token = getToken();

        UUID id = createTransaction(token);

        mockMvc.perform(
                        get("/api/v1/transactions/{id}", id)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(id.toString())
                );
    }

    @Test
    void shouldDeleteTransaction() throws Exception {

        String token = getToken();

        UUID id = createTransaction(token);

        mockMvc.perform(
                        delete("/api/v1/transactions/{id}", id)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldCreateExpenseWithPaymentMethod() throws Exception {

        String token = getToken();

        Category category = createExpenseCategory();

        UUID accountId = createFinancialAccount(token);

        String body = """
            {
                "description":"Supermercado",
                "amount":250.00,
                "transactionDate":"2026-07-08",
                "type":"EXPENSE",
                "paymentMethod":"PIX",
                "accountId":"%s",
                "categoryId":"%s",
                "subCategoryId":null
            }
            """.formatted(accountId, category.getId());

        mockMvc.perform(
                        post("/api/v1/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.paymentMethod").value("PIX"))
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.accountName").value("Transaction Test Account"));
    }

    @Test
    void shouldRejectTransactionWithoutPaymentMethod() throws Exception {

        String token = getToken();

        UUID accountId = createFinancialAccount(token);

        Category category = createExpenseCategory();

        String body = """
            {
                "description":"Supermercado",
                "amount":250.00,
                "transactionDate":"2026-07-08",
                "type":"EXPENSE",
                "accountId":"%s",
                "categoryId":"%s",
                "subCategoryId":null
            }
            """.formatted(accountId, category.getId());

        mockMvc.perform(
                        post("/api/v1/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Payment method is required for all transactions."
                        ));
    }

    @Test
    void shouldRejectIncomeWithDebitCard() throws Exception {

        String token = getToken();

        UUID accountId = createFinancialAccount(token);

        Category category = categoryRepository
                .findByName("Receita")
                .orElseThrow();

        SubCategory subCategory = subCategoryRepository
                .findByName("Salário")
                .orElseThrow();

        String body = """
            {
                "description":"Salário",
                "amount":4500.00,
                "transactionDate":"2026-07-08",
                "type":"INCOME",
                "paymentMethod":"DEBIT_CARD",
                "accountId":"%s",
                "categoryId":"%s",
                "subCategoryId":"%s"
            }
            """.formatted(
                accountId,
                category.getId(),
                subCategory.getId()
        );

        mockMvc.perform(
                        post("/api/v1/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Debit card is not allowed for income transactions."
                        ));
    }

    @Test
    void shouldRejectTransactionWithFinancialAccountFromAnotherUser()
            throws Exception {

        String firstUserToken = getToken();

        String secondUserToken = getToken(
                "user.two@example.test",
                "test-password"
        );

        UUID accountId = createFinancialAccount(firstUserToken);

        Category category = categoryRepository
                .findByName("Receita")
                .orElseThrow();

        SubCategory subCategory = subCategoryRepository
                .findByName("Salário")
                .orElseThrow();

        String body = """
            {
                "description":"Invalid Account Ownership",
                "amount":1000.00,
                "transactionDate":"2026-07-10",
                "type":"INCOME",
                "paymentMethod":"BANK_TRANSFER",
                "accountId":"%s",
                "categoryId":"%s",
                "subCategoryId":"%s"
            }
            """.formatted(
                accountId,
                category.getId(),
                subCategory.getId()
        );

        mockMvc.perform(
                        post("/api/v1/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + secondUserToken
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.message")
                                .value("Financial account not found.")
                );
    }

    @Test
    void shouldRejectUpdateWithFinancialAccountFromAnotherUser()
            throws Exception {

        String firstUserToken = getToken();

        String secondUserToken = getToken(
                "user.two@example.test",
                "test-password"
        );

        UUID transactionId = createTransaction(firstUserToken);
        UUID secondUserAccountId =
                createFinancialAccount(secondUserToken);

        Category category = categoryRepository
                .findByName("Receita")
                .orElseThrow();

        SubCategory subCategory = subCategoryRepository
                .findByName("Salário")
                .orElseThrow();

        String body = """
            {
                "description":"Updated Salary",
                "amount":12000.00,
                "transactionDate":"2026-07-10",
                "type":"INCOME",
                "paymentMethod":"BANK_TRANSFER",
                "accountId":"%s",
                "categoryId":"%s",
                "subCategoryId":"%s"
            }
            """.formatted(
                secondUserAccountId,
                category.getId(),
                subCategory.getId()
        );

        mockMvc.perform(
                        put(
                                "/api/v1/transactions/{id}",
                                transactionId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + firstUserToken
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.message")
                                .value("Financial account not found.")
                );
    }
}