package com.ronney.finance.controller;

import com.ronney.finance.BaseIntegrationTest;
import com.ronney.finance.domain.entity.User;
import com.ronney.finance.dto.request.LoginRequest;
import com.ronney.finance.dto.request.RegisterRequest;
import com.ronney.finance.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIT extends BaseIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest(
                "user.one@example.test",
                "test-password"
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
                        "user.one@example.test",
                        "invalid-password"
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

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "Ronney Rocha",
                "Rocha Family",
                "ronney@example.test",
                "password123"
        );

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isCreated());

        User user = userRepository.findByEmailWithHousehold(request.email())
                .orElseThrow();

        assertEquals(request.name(), user.getName());
        assertEquals(request.email(), user.getEmail());

        assertNotNull(user.getHousehold());
        assertEquals(request.householdName(), user.getHousehold().getName());

        assertTrue(
                passwordEncoder.matches(
                        request.password(),
                        user.getPassword()
                )
        );
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "Another User",
                "Another Family",
                "user.one@example.test",
                "password123"
        );

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "",
                "",
                "invalid-email",
                "123"
        );

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
