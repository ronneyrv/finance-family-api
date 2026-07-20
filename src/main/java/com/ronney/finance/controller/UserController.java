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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}