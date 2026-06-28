package com.ronney.finance.dto.response;

import java.math.BigDecimal;

public record CreditCardSummaryResponse(
        String card,
        BigDecimal creditLimit,
        BigDecimal usedLimit,
        BigDecimal availableLimit,
        Integer openInstallments
) {
}
