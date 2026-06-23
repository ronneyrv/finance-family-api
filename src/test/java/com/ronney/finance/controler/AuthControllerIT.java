package com.ronney.finance.controler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ronney.finance.BaseIntegrationTest;
import com.ronney.finance.dto.request.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void sholdLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest(
                "ronneyrv@gmail.com",
                "123456"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)
                )
        )
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnUnauthorizedForInvalidPassword()
            throws Exception {

        LoginRequest request =
                new LoginRequest(
                        "ronneyrv@gmail.com",
                        "999999"
                );

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
        )
        .andDo(print())
        .andExpect(status().isUnauthorized());
    }
}
