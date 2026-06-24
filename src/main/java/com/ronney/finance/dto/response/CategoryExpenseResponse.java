package com.ronney.finance.dto.response;

import java.math.BigDecimal;

public record CategoryExpenseResponse(
        String category,
        BigDecimal amount
) {
}
