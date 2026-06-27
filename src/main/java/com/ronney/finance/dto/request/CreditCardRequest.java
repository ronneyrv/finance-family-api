package com.ronney.finance.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreditCardRequest(
        @NotBlank
        String name,

        @NotNull
        @Positive
        BigDecimal creditLimit,

        @Min(1)
        @Max(31)
        Integer closingDay,

        @Min(1)
        @Max(31)
        Integer dueDay
) {
}
