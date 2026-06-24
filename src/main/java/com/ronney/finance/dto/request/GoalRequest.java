package com.ronney.finance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalRequest(
        @NotBlank
        String name,

        @NotNull
        @Positive
        BigDecimal targetAmount,

        LocalDate targetDate
) {
}
