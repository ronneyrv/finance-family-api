package com.ronney.finance.dto.request;

import com.ronney.finance.domain.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FinancialAccountRequest(
        @Schema(
                description = "Financial account name",
                example = "Nubank Account"
        )
        @NotBlank
        String name,

        @Schema(
                description = "Financial account type",
                example = "DIGITAL_ACCOUNT",
                allowableValues = {
                        "CHECKING_ACCOUNT",
                        "SAVINGS_ACCOUNT",
                        "DIGITAL_ACCOUNT",
                        "CASH"
                }
        )
        @NotNull
        AccountType accountType,

        @Schema(
                description = "Initial account balance",
                example = "2500.00"
        )
        @NotNull
        BigDecimal initialBalance
) {
}