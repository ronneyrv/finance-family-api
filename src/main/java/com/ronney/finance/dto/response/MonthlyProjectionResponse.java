package com.ronney.finance.dto.response;

import java.math.BigDecimal;
import java.time.Month;

public record MonthlyProjectionResponse(
        Month month,
        BigDecimal projectedIncome,
        BigDecimal projectedRecurringExpense,
        BigDecimal projectedCreditCardExpense,
        BigDecimal projectedTotalExpense,
        BigDecimal projectedBalance
) {
}