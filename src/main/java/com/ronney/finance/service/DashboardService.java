package com.ronney.finance.service;

import com.ronney.finance.dto.response.*;

import java.util.List;

public interface DashboardService {
    DashboardSummaryResponse getSummary();

    DashboardFiltersResponse getFilters();

    List<CategoryExpenseResponse> getExpensesByCategory();

    List<MonthlySummaryResponse> getMonthlySummary(
            Integer year
    );

    List<MonthlyProjectionResponse> getProjection(
            Integer year
    );

    List<CreditCardInvoiceSummaryResponse> getCreditCardSummaries();
}
