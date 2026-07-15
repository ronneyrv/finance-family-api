package com.ronney.finance.dto.response;

import java.util.List;

public record DashboardFiltersResponse(
        List<Integer> years,
        List<Integer> months,
        Integer defaultYear,
        Integer defaultMonth
) {
}