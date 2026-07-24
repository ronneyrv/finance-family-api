package com.ronney.finance.controller;

import com.ronney.finance.dto.request.UpdateCurrentUserRequest;
import com.ronney.finance.dto.response.CurrentUserResponse;
import com.ronney.finance.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

@Tag(
        name = "Users",
        description = "Manage authenticated user profile."
)
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Get current user",
            description = "Returns the authenticated user's profile."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User returned successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @GetMapping("/me")
    public CurrentUserResponse getCurrentUser() {

        return userService.getCurrentUser();
    }

    @Operation(
            summary = "Update current user",
            description = "Updates the authenticated user's profile."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PutMapping("/me")
    public CurrentUserResponse updateCurrentUser(

            @Valid
            @RequestBody
            UpdateCurrentUserRequest request
    ) {

        return userService.updateCurrentUser(request);
    }

    @Operation(
            summary = "Upload user avatar",
            description = "Uploads or replaces the authenticated user's avatar."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Avatar uploaded successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid image"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PostMapping(
            value = "/me/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public CurrentUserResponse uploadAvatar(
            @RequestParam("file") MultipartFile file
    ) {
        return userService.uploadAvatar(file);
    }
}