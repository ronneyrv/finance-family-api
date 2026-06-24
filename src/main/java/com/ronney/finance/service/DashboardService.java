package com.ronney.finance.service;

import com.ronney.finance.dto.response.CategoryExpenseResponse;
import com.ronney.finance.dto.response.DashboardSummaryResponse;

import java.util.List;

public interface DashboardService {
    DashboardSummaryResponse getSummary();

    List<CategoryExpenseResponse> getExpensesByCategory();
}
