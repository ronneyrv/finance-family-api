package com.ronney.finance.repository.projection;

import java.math.BigDecimal;

public interface MonthlySummaryProjection {
    Integer getMonth();

    BigDecimal getIncome();

    BigDecimal getExpense();
}
