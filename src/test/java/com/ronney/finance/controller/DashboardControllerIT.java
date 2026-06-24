package com.ronney.finance.controller;

import com.ronney.finance.BaseIntegrationTest;
import com.ronney.finance.domain.entity.Category;
import com.ronney.finance.domain.entity.SubCategory;
import com.ronney.finance.repository.CategoryRepository;
import com.ronney.finance.repository.SubCategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DashboardControllerIT extends BaseIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    private void createIncome(
            String token,
            int amount
    ) throws Exception {
        Category category =
                categoryRepository
                        .findByName("Receita")
                        .orElseThrow();
        SubCategory subCategory =
                subCategoryRepository
                        .findByName("Salário")
                        .orElseThrow();
        String body = """
            {
                "description":"Salário",
                "amount":%d,
                "transactionDate":"2026-07-01",
                "type":"INCOME",
                "categoryId":"%s",
                "subCategoryId":"%s"
            }
            """.formatted(
                amount,
                category.getId(),
                subCategory.getId()
        );
        mockMvc.perform(
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
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnDashboardSummary()
            throws Exception {
        String token = getToken();
        createIncome(token, 10000);
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
                                .value(10000)
                );
    }

    @Test
    void shouldReturnExpensesByCategory()
            throws Exception {
        String token = getToken();
        mockMvc.perform(
                        get("/api/v1/dashboard/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }
}
