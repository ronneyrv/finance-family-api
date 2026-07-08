package com.ronney.finance.controller;

import com.ronney.finance.BaseIntegrationTest;
import com.ronney.finance.domain.entity.Category;
import com.ronney.finance.domain.entity.SubCategory;
import com.ronney.finance.repository.CategoryRepository;
import com.ronney.finance.repository.SubCategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RecurringTransactionControllerIT extends BaseIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    private UUID createRecurringTransaction(
            String token
    ) throws Exception {

        Category category = categoryRepository
                .findByName("Receita")
                .orElseThrow();

        SubCategory subCategory = subCategoryRepository
                .findByName("Salário")
                .orElseThrow();

        String body = """
                {
                    "description":"Salário mensal",
                    "amount":10000,
                    "type":"INCOME",
                    "paymentMethod":"BANK_TRANSFER",
                    "dayOfMonth":5,
                    "startDate":"2026-07-01",
                    "endDate":null,
                    "categoryId":"%s",
                    "subCategoryId":"%s"
                }
                """.formatted(
                category.getId(),
                subCategory.getId()
        );

        String response = mockMvc.perform(
                        post("/api/v1/recurring-transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.description")
                                .value("Salário mensal")
                )
                .andExpect(
                        jsonPath("$.active")
                                .value(true)
                )
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
    void shouldCreateRecurringTransaction()
            throws Exception {

        String token = getToken();

        UUID id = createRecurringTransaction(token);

        mockMvc.perform(
                        get(
                                "/api/v1/recurring-transactions/{id}",
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
                        jsonPath("$.paymentMethod")
                                .value("BANK_TRANSFER")
                )
                .andExpect(
                        jsonPath("$.dayOfMonth")
                                .value(5)
                );
    }

    @Test
    void shouldListRecurringTransactions()
            throws Exception {

        String token = getToken();

        createRecurringTransaction(token);

        mockMvc.perform(
                        get("/api/v1/recurring-transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    void shouldFindRecurringTransactionById()
            throws Exception {

        String token = getToken();

        UUID id = createRecurringTransaction(token);

        mockMvc.perform(
                        get(
                                "/api/v1/recurring-transactions/{id}",
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
                        jsonPath("$.description")
                                .value("Salário mensal")
                );
    }

    @Test
    void shouldUpdateRecurringTransaction()
            throws Exception {

        String token = getToken();

        UUID id = createRecurringTransaction(token);

        Category category = categoryRepository
                .findByName("Receita")
                .orElseThrow();

        SubCategory subCategory = subCategoryRepository
                .findByName("Salário")
                .orElseThrow();

        String body = """
                {
                    "description":"Salário atualizado",
                    "amount":12000,
                    "type":"INCOME",
                    "paymentMethod":"BANK_TRANSFER",
                    "dayOfMonth":10,
                    "startDate":"2026-07-01",
                    "endDate":null,
                    "categoryId":"%s",
                    "subCategoryId":"%s"
                }
                """.formatted(
                category.getId(),
                subCategory.getId()
        );

        mockMvc.perform(
                        put(
                                "/api/v1/recurring-transactions/{id}",
                                id
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.description")
                                .value("Salário atualizado")
                )
                .andExpect(
                        jsonPath("$.amount")
                                .value(12000)
                )
                .andExpect(
                        jsonPath("$.dayOfMonth")
                                .value(10)
                );
    }

    @Test
    void shouldDeleteRecurringTransaction()
            throws Exception {

        String token = getToken();

        UUID id = createRecurringTransaction(token);

        mockMvc.perform(
                        delete(
                                "/api/v1/recurring-transactions/{id}",
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
                                "/api/v1/recurring-transactions/{id}",
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
    void shouldRejectEndDateBeforeStartDate()
            throws Exception {

        String token = getToken();

        Category category = categoryRepository
                .findByName("Receita")
                .orElseThrow();

        String body = """
            {
                "description":"Receita inválida",
                "amount":5000,
                "type":"INCOME",
                "paymentMethod":"BANK_TRANSFER",
                "dayOfMonth":5,
                "startDate":"2026-07-01",
                "endDate":"2026-06-30",
                "categoryId":"%s",
                "subCategoryId":null
            }
            """.formatted(category.getId());

        mockMvc.perform(
                        post("/api/v1/recurring-transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "End date must not be before start date."
                                )
                );
    }

    @Test
    void shouldRejectIncomeWithDebitCard()
            throws Exception {

        String token = getToken();

        Category category = categoryRepository
                .findByName("Receita")
                .orElseThrow();

        String body = """
            {
                "description":"Receita inválida",
                "amount":5000,
                "type":"INCOME",
                "paymentMethod":"DEBIT_CARD",
                "dayOfMonth":5,
                "startDate":"2026-07-01",
                "endDate":null,
                "categoryId":"%s",
                "subCategoryId":null
            }
            """.formatted(category.getId());

        mockMvc.perform(
                        post("/api/v1/recurring-transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Debit card is not allowed for income transactions."
                                )
                );
    }

    @Test
    void shouldRejectCategoryWithDifferentTransactionType()
            throws Exception {

        String token = getToken();

        Category category = categoryRepository
                .findByName("Receita")
                .orElseThrow();

        String body = """
            {
                "description":"Despesa inválida",
                "amount":500,
                "type":"EXPENSE",
                "paymentMethod":"PIX",
                "dayOfMonth":10,
                "startDate":"2026-07-01",
                "endDate":null,
                "categoryId":"%s",
                "subCategoryId":null
            }
            """.formatted(category.getId());

        mockMvc.perform(
                        post("/api/v1/recurring-transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Category does not match transaction type."
                                )
                );
    }

    @Test
    void shouldNotAllowUserToAccessAnotherUsersRecurringTransaction()
            throws Exception {

        String userOneToken = getToken();

        String userTwoToken = getToken(
                "user.two@example.test",
                "test-password"
        );

        UUID recurringTransactionId =
                createRecurringTransaction(userOneToken);

        mockMvc.perform(
                        get(
                                "/api/v1/recurring-transactions/{id}",
                                recurringTransactionId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + userTwoToken
                                )
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeactivateRecurringTransaction()
            throws Exception {

        String token = getToken();

        UUID id = createRecurringTransaction(token);

        mockMvc.perform(
                        patch(
                                "/api/v1/recurring-transactions/{id}/status",
                                id
                        )
                                .param("active", "false")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.active")
                                .value(false)
                );

        mockMvc.perform(
                        get(
                                "/api/v1/recurring-transactions/{id}",
                                id
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.active")
                                .value(false)
                );
    }
}