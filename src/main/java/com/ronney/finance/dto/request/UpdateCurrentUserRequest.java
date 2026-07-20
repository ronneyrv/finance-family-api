package com.ronney.finance.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateCurrentUserRequest(

        @NotBlank(message = "Name is required.")
        String name

) {
}