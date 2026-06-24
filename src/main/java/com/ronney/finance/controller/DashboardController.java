package com.ronney.finance.controller;

import com.ronney.finance.dto.response.CategoryExpenseResponse;
import com.ronney.finance.dto.response.DashboardSummaryResponse;
import com.ronney.finance.dto.response.MonthlySummaryResponse;
import com.ronney.finance.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/categories")
    public List<CategoryExpenseResponse> getExpensesByCategory() {
        return dashboardService.getExpensesByCategory();
    }

    @GetMapping("/monthly")
    public List<MonthlySummaryResponse> getMonthlySummary(@RequestParam Integer year) {
        return dashboardService.getMonthlySummary(year);
    }
}
