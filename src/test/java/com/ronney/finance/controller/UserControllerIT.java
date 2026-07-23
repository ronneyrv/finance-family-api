package com.ronney.finance.controller;

import com.ronney.finance.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Test
    void shouldUploadAvatar() throws Exception {

        String token = getToken();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/v1/users/me/avatar")
                                .file(file)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").exists())
                .andExpect(jsonPath("$.avatarUrl").isNotEmpty());
    }

    @Test
    void shouldRejectEmptyAvatar() throws Exception {

        String token = getToken();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        mockMvc.perform(
                        multipart("/api/v1/users/me/avatar")
                                .file(file)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Avatar file must not be empty."));
    }

    @Test
    void shouldRejectInvalidAvatarContentType() throws Exception {

        String token = getToken();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "hello".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/v1/users/me/avatar")
                                .file(file)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Only JPEG, PNG and WEBP images are allowed."));
    }

    @Test
    void shouldRejectAvatarUploadWhenUserIsNotAuthenticated() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/v1/users/me/avatar")
                                .file(file)
                )
                .andExpect(status().isForbidden());
    }
}