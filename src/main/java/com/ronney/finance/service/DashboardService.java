package com.ronney.finance.service;

import com.ronney.finance.dto.response.*;

import java.util.List;

public interface DashboardService {
    DashboardSummaryResponse getSummary();

    FinancialHealthResponse getFinancialHealth();

    DashboardFiltersResponse getFilters();

    List<CategoryExpenseResponse> getExpensesByCategory();

    List<MonthlySummaryResponse> getMonthlySummary( Integer year );

    List<MonthlyProjectionResponse> getProjection( Integer year );

    List<CashFlowResponse> getCashFlow( Integer year );

    List<CumulativeResultResponse> getCumulativeResult( Integer year );

    List<CumulativeResultResponse> getMyCumulativeResult(Integer year);

    List<CreditCardInvoiceSummaryResponse> getCreditCardSummaries();

    List<MonthlyCreditCardTrendResponse> getCreditCardTrend( Integer year );

    IncomeCommitmentResponse getIncomeCommitment();
}
