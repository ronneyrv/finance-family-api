package com.ronney.finance.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 100)
        String name,

        @NotBlank(message = "Household name is required")
        @Size(min = 3, max = 100)
        String householdName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 255)
        String password
) {
}