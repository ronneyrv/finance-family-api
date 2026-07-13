package com.ronney.finance.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.ronney.finance.BaseIntegrationTest;
import com.ronney.finance.domain.entity.RefreshToken;
import com.ronney.finance.domain.entity.User;
import com.ronney.finance.dto.request.LoginRequest;
import com.ronney.finance.dto.request.RefreshTokenRequest;
import com.ronney.finance.dto.request.RegisterRequest;
import com.ronney.finance.repository.RefreshTokenRepository;
import com.ronney.finance.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIT extends BaseIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private String loginAndGetRefreshToken() throws Exception {

        LoginRequest request = new LoginRequest(
                "user.one@example.test",
                "test-password"
        );

        MvcResult result = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        assertNotNull(response.get("accessToken"));
        assertNotNull(response.get("refreshToken"));

        assertFalse(response.get("accessToken").asText().isBlank());
        assertFalse(response.get("refreshToken").asText().isBlank());

        return response.get("refreshToken").asText();
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {

        LoginRequest request = new LoginRequest(
                "user.one@example.test",
                "test-password"
        );

        MvcResult result = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        assertNotNull(response.get("accessToken"));
        assertNotNull(response.get("refreshToken"));

        assertFalse(response.get("accessToken").asText().isBlank());
        assertFalse(response.get("refreshToken").asText().isBlank());
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

    @Test
    void shouldRefreshAccessToken() throws Exception {

        String refreshToken = loginAndGetRefreshToken();

        RefreshTokenRequest request =
                new RefreshTokenRequest(refreshToken);

        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/auth/refresh")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        request
                                                )
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn();

        JsonNode response =
                objectMapper.readTree(
                        result.getResponse()
                                .getContentAsString()
                );

        assertNotNull(response.get("accessToken"));

        assertFalse(
                response.get("accessToken")
                        .asText()
                        .isBlank()
        );
    }

    @Test
    void shouldReturnUnauthorizedForInvalidRefreshToken() throws Exception {

        RefreshTokenRequest request =
                new RefreshTokenRequest("invalid-refresh-token");

        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedForRevokedRefreshToken() throws Exception {

        String token = loginAndGetRefreshToken();

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByToken(token)
                        .orElseThrow();

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);

        RefreshTokenRequest request =
                new RefreshTokenRequest(token);

        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedForExpiredRefreshToken() throws Exception {

        String token = loginAndGetRefreshToken();

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByToken(token)
                        .orElseThrow();

        refreshToken.setExpiresAt(
                Instant.now().minusSeconds(60)
        );

        refreshTokenRepository.save(refreshToken);

        RefreshTokenRequest request =
                new RefreshTokenRequest(token);

        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
