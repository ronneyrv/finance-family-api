package com.ronney.finance.repository;

import java.math.BigDecimal;

public interface SubCategoryAmountProjection {

    String getSubCategory();

    BigDecimal getAmount();
}