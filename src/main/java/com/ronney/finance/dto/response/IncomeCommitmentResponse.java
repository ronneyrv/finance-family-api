package com.ronney.finance.dto.response;

import com.ronney.finance.domain.enums.CommitmentLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record IncomeCommitmentResponse(

        @Schema(
                description = "Total recurring monthly household income",
                example = "12000.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal monthlyIncome,

        @Schema(
                description = "Total recurring monthly household expenses",
                example = "2800.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal recurringExpenses,

        @Schema(
                description = "Total unpaid credit card installments",
                example = "2000.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal unpaidCreditCardInstallments,

        @Schema(
                description = "Total monthly household commitments",
                example = "4800.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal monthlyCommitments,

        @Schema(
                description = "Remaining monthly household income after commitments",
                example = "7200.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal availableIncome,

        @Schema(
                description = "Percentage of recurring income already committed",
                example = "40.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal commitmentPercentage,

        CommitmentLevel commitmentLevel

) {
}