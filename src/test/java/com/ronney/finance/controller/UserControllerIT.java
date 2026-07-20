package com.ronney.finance.controller;

import com.ronney.finance.BaseIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIT extends BaseIntegrationTest {

    @Test
    void shouldReturnCurrentUser() throws Exception {

        String token = getToken();

        mockMvc.perform(
                        get("/api/v1/users/me")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test User One"))
                .andExpect(jsonPath("$.email").value("user.one@example.test"));
    }

    @Test
    void shouldUpdateCurrentUser() throws Exception {

        String token = getToken();

        String body = """
        {
            "name": "Updated User"
        }
        """;

        mockMvc.perform(
                        put("/api/v1/users/me")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated User"))
                .andExpect(jsonPath("$.email").value("user.one@example.test"));
    }

    @Test
    void shouldReturnForbiddenWhenUserIsNotAuthenticated() throws Exception {

        mockMvc.perform(
                        get("/api/v1/users/me")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectInvalidUpdateRequest() throws Exception {

        String token = getToken();

        String body = """
        {
            "name": ""
        }
        """;

        mockMvc.perform(
                        put("/api/v1/users/me")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest());
    }
}