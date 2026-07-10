package com.ronney.finance.dto.response;

import com.ronney.finance.domain.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

public record FinancialAccountResponse(
        @Schema(
                description = "Financial account identifier",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        UUID id,

        @Schema(
                description = "Financial account name",
                example = "Nubank Account",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String name,

        @Schema(
                description = "Financial account type",
                example = "DIGITAL_ACCOUNT",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        AccountType accountType,

        @Schema(
                description = "Initial account balance",
                example = "2500.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal initialBalance,

        @Schema(
                description = "Current account balance",
                example = "2000.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
                BigDecimal currentBalance
) {
}