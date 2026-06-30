package com.ronney.finance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreditCardRequest(
        @Schema(
                description = "Credit card name",
                example = "Nubank Platinum"
        )
        @NotBlank
        String name,

        @Schema(
                description = "Credit limit",
                example = "10000.00"
        )
        @NotNull
        @Positive
        BigDecimal creditLimit,

        @Schema(
                description = "Invoice closing day",
                example = "20"
        )
        @Min(1)
        @Max(31)
        Integer closingDay,

        @Schema(
                description = "Invoice due day",
                example = "28"
        )
        @Min(1)
        @Max(31)
        Integer dueDay
) {
}
