package com.ronney.finance.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseRequest(
        @NotBlank
        String description,

        @NotNull
        @Positive
        BigDecimal totalAmount,

        @NotNull
        @Min(1)
        @Max(36)
        Integer installments,

        @NotNull
        LocalDate purshaseDate
) {
}
