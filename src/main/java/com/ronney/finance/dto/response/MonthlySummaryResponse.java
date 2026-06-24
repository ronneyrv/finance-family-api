package com.ronney.finance.dto.response;

import java.math.BigDecimal;
import java.time.Month;

public record MonthlySummaryResponse(
        Month month,
        BigDecimal income,
        BigDecimal expense
) {
}
