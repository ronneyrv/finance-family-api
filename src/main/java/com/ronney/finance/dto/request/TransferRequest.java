package com.ronney.finance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransferRequest(
        @Schema(
                description = "Transfer description",
                example = "Monthly savings transfer"
        )
        @NotBlank
        String description,

        @Schema(
                description = "Transfer amount",
                example = "1000.00"
        )
        @NotNull
        @Positive
        BigDecimal amount,

        @Schema(
                description = "Transfer date",
                example = "2026-09-25"
        )
        @NotNull
        LocalDate transactionDate,

        @Schema(
                description = "Source financial account identifier",
                example = "4d0df1d8-8b62-4c0e-bef8-7dbfb74b27f6"
        )
        @NotNull
        UUID sourceAccountId,

        @Schema(
                description = "Destination financial account identifier",
                example = "fd18d65e-87df-4a6e-aef5-d4b7fd0a2b8d"
        )
        @NotNull
        UUID destinationAccountId
) {
}