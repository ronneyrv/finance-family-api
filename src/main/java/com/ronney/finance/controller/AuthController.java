package com.ronney.finance.controller;

import com.ronney.finance.dto.request.LoginRequest;
import com.ronney.finance.dto.request.RegisterRequest;
import com.ronney.finance.dto.response.LoginResponse;
import com.ronney.finance.dto.response.RegisterResponse;
import com.ronney.finance.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Authentication",
        description = "Endpoints for user authentication"
)
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "Authenticate user",
            description = "Authenticates a user and returns a JWT access token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid
            @RequestBody
            LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
            summary = "Register user",
            description = "Creates a new user account and automatically creates the user's household."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User successfully created"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            )
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid
            @RequestBody
            RegisterRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    }
}
