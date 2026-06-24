package com.ronney.finance.repository;

import java.math.BigDecimal;

public interface CategoryExpenseProjection {
    String getCategory();

    BigDecimal getAmount();
}
