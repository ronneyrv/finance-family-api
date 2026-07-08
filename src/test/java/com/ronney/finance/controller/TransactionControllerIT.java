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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionControllerIT extends BaseIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    private UUID createTransaction(String token) throws Exception {

        Category category = categoryRepository
                        .findByName("Receita")
                        .orElseThrow();

        SubCategory subCategory = subCategoryRepository
                        .findByName("Salário")
                        .orElseThrow();

        String body = """
                {
                    "description":"Salário Junho",
                    "amount":10000,
                    "transactionDate":"2026-06-23",
                    "type":"INCOME",
                    "paymentMethod":"BANK_TRANSFER",
                    "categoryId":"%s",
                    "subCategoryId":"%s"
                }
                """.formatted(
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
                                                MediaType.APPLICATION_JSON
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

        String body = """
            {
                "description":"Supermercado",
                "amount":250.00,
                "transactionDate":"2026-07-08",
                "type":"EXPENSE",
                "paymentMethod":"PIX",
                "categoryId":"%s",
                "subCategoryId":null
            }
            """.formatted(category.getId());

        mockMvc.perform(
                        post("/api/v1/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.paymentMethod").value("PIX"));
    }

    @Test
    void shouldRejectTransactionWithoutPaymentMethod() throws Exception {

        String token = getToken();

        Category category = createExpenseCategory();

        String body = """
            {
                "description":"Supermercado",
                "amount":250.00,
                "transactionDate":"2026-07-08",
                "type":"EXPENSE",
                "categoryId":"%s",
                "subCategoryId":null
            }
            """.formatted(category.getId());

        mockMvc.perform(
                        post("/api/v1/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
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
                "categoryId":"%s",
                "subCategoryId":"%s"
            }
            """.formatted(
                category.getId(),
                subCategory.getId()
        );

        mockMvc.perform(
                        post("/api/v1/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Debit card is not allowed for income transactions."
                        ));
    }
}