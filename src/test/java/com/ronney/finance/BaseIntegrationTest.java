package com.ronney.finance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ronney.finance.config.TestDataInitializer;
import com.ronney.finance.dto.request.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDataInitializer.class)
public abstract class BaseIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String getToken() throws Exception {
        return getToken(
                "user.one@example.test",
                "test-password"
        );
    }

    protected String getToken(
            String email,
            String password
    ) throws Exception {

        LoginRequest request =
                new LoginRequest(
                        email,
                        password
                );

        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        request
                                                )
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn();

        String response =
                result.getResponse()
                        .getContentAsString();

        return objectMapper
                .readTree(response)
                .get("accessToken")
                .asText();
    }
}
