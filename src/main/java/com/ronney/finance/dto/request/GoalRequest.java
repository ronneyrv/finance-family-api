package com.ronney.finance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalRequest(
        @Schema(
                description = "Goal name",
                example = "Trip to Europe"
        )
        @NotBlank
        String name,

        @Schema(
                description = "Target amount",
                example = "30000.00"
        )
        @NotNull
        @Positive
        BigDecimal targetAmount,

        @Schema(
                description = "Target date",
                example = "2027-07-01"
        )
        LocalDate targetDate
) {
}
