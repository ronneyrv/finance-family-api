package com.ronney.finance.controller;

import com.ronney.finance.dto.response.CategoryExpenseResponse;
import com.ronney.finance.dto.response.DashboardSummaryResponse;
import com.ronney.finance.dto.response.MonthlySummaryResponse;
import com.ronney.finance.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(
        name = "Dashboard",
        description = "Financial dashboard and analytics."
)
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @Operation(
            summary = "Get financial dashboard",
            description = """
                Returns a financial summary for the authenticated user.

                Includes:
                - Total income
                - Total expenses
                - Current balance
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Dashboard generated successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary() {
        return dashboardService.getSummary();
    }

    @Operation(
            summary = "Get expenses by category",
            description = """
                Returns the total amount of expenses grouped by category
                for the authenticated user.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Category expense summary generated successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @GetMapping("/categories")
    public List<CategoryExpenseResponse> getExpensesByCategory() {
        return dashboardService.getExpensesByCategory();
    }

    @Operation(
            summary = "Get monthly financial summary",
            description = """
                Returns the monthly evolution of income,
                expenses and balance for the authenticated user.
                """
    )
    @GetMapping("/monthly")
    public List<MonthlySummaryResponse> getMonthlySummary(
            @Parameter(
                    description = "Year used to generate the monthly summary",
                    example = "2026"
            )
            @RequestParam Integer year
    ) {
        return dashboardService.getMonthlySummary(year);
    }
}
