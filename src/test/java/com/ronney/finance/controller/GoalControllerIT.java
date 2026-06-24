package com.ronney.finance.controller;

import com.ronney.finance.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GoalControllerIT extends BaseIntegrationTest {
    private UUID createGoal(
            String token
    ) throws Exception {

        String body = """
            {
                "name":"Viagem para Europa",
                "targetAmount":30000,
                "targetDate":"2027-07-01"
            }
            """;

        String response = mockMvc.perform(post("/api/v1/goals")
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

        return UUID.fromString(
                objectMapper
                        .readTree(response)
                        .get("id")
                        .asText()
        );
    }

    @Test
    void shouldCreateGoal()
            throws Exception {

        String token = getToken();

        UUID id = createGoal(token);

        assertNotNull(id);
    }

    @Test
    void shouldListGoals()
            throws Exception {

        String token = getToken();

        createGoal(token);

        mockMvc.perform(get("/api/v1/goals")
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
    void shouldFindGoalById()
            throws Exception {

        String token = getToken();

        UUID id = createGoal(token);

        mockMvc.perform(get("/api/v1/goals/{id}", id)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString())
                );
    }

    @Test
    void shouldUpdateGoal()
            throws Exception {

        String token = getToken();

        UUID id = createGoal(token);

        String body = """
            {
                "name":"Viagem para Japão",
                "targetAmount":40000,
                "targetDate":"2028-01-01"
            }
            """;

        mockMvc.perform(put("/api/v1/goals/{id}", id)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Viagem para Japão")
                );
    }

    @Test
    void shouldDeleteGoal()
            throws Exception {

        String token = getToken();

        UUID id = createGoal(token);

        mockMvc.perform(delete("/api/v1/goals/{id}", id)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/goals/{id}", id)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNotFound());
    }
}
