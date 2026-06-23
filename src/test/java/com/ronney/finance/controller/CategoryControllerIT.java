package com.ronney.finance.controller;

import com.ronney.finance.BaseIntegrationTest;
import com.ronney.finance.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CategoryControllerIT extends BaseIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldListCategories() throws Exception {

        String token = getToken();

        mockMvc.perform(
                    get("/api/v1/categories")
                            .header(
                                    "Authorization",
                                    "Bearer " + token
                            )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldFilterCategoriesByType()
            throws Exception {

        String token = getToken();

        mockMvc.perform(
                get("/api/v1/categories")
                        .param("type", "INCOME")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldListSubCategories()
            throws Exception {

        String token = getToken();

        UUID receitaId =
            categoryRepository
                    .findAll()
                    .stream()
                    .filter(
                            category ->
                                    category
                                            .getName()
                                            .equals("Receita")
                    )
                    .findFirst()
                    .orElseThrow()
                    .getId();

        mockMvc.perform(
                    get(
                            "/api/v1/categories/{id}/sub-categories",
                            receitaId
                    )
                            .header(
                                    "Authorization",
                                    "Bearer " + token
                            )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}
