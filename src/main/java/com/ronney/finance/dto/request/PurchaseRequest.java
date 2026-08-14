package com.ronney.finance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PurchaseRequest(
        @Schema(
                description = "Purchase description",
                example = "MacBook Pro M4"
        )
        @NotBlank
        String description,

        @Schema(
                description = "Purchase amount",
                example = "12000.00"
        )
        @NotNull
        @Positive
        BigDecimal totalAmount,

        @Schema(
                description = "Number of installments",
                example = "12"
        )
        @NotNull
        @Min(1)
        @Max(36)
        Integer installments,

        @Schema(
                description = "Purchase date",
                example = "2026-09-25"
        )
        @NotNull
        LocalDate purchaseDate,

        @Schema(
                description = "Category identifier",
                example = "4d0df1d8-8b62-4c0e-bef8-7dbfb74b27f6"
        )
                UUID categoryId,

        @Schema(
                description = "Subcategory identifier",
                example = "fd18d65e-87df-4a6e-aef5-d4b7fd0a2b8d"
        )
        UUID subCategoryId
) {
}
