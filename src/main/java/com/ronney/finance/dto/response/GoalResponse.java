package com.ronney.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GoalResponse(
        @Schema(
                description = "Goal identifier",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        UUID id,

        @Schema(
                description = "Goal name",
                example = "Trip to Europe",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String name,

        @Schema(
                description = "Target amount",
                example = "30000.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal targetAmount,

        @Schema(
                description = "Current saved amount",
                example = "8500.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal currentAmount,
        @Schema(
                description = "Remaining amount to reach the goal",
                example = "21500.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal remainingAmount,

        @Schema(
                description = "Goal progress percentage",
                example = "28",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Integer progress,

        @Schema(
                description = "Target completion date",
                example = "2027-07-01",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        LocalDate targetDate
) {
}
