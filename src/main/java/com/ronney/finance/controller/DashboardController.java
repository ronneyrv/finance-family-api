package com.ronney.finance.controller;

import com.ronney.finance.dto.response.*;
import com.ronney.finance.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
                    responseCode = "400",
                    description = "Not Found"
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
            summary = "Get household financial health",
            description = """
            Returns the current financial health of the authenticated user's household.
            Includes:
            - Total household assets
            - Total household liabilities
            - Current household net worth
            - Financial health score
            - Financial health level
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Financial health retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @GetMapping("/financial-health")
    public ResponseEntity<FinancialHealthResponse> getFinancialHealth() {
        return ResponseEntity.ok(
                dashboardService.getFinancialHealth()
        );
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
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Financial summary returned successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Financial summary not found"
            )
    })
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

    @Operation(
            summary = "Get financial projection",
            description = """
                Returns the monthly financial projection for the selected year.

                Includes recurring income, recurring expenses,
                credit card installments and projected balance.
                """
    )
    @GetMapping("/projection")
    public List<MonthlyProjectionResponse> getProjection(
            @RequestParam Integer year
    ) {
        return dashboardService.getProjection(year);
    }

    @Operation(
            summary = "Get dashboard filters",
            description = """
            Returns the available filters for the financial dashboard.

            Includes:
            - Available years
            - Available months
            - Default year
            - Default month
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Dashboard filters returned successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @GetMapping("/filters")
    public DashboardFiltersResponse getFilters() {
        return dashboardService.getFilters();
    }

    @Operation(
            summary = "Get current credit card invoice summary",
            description = """
            Returns a summary of the current invoice for each
            credit card belonging to the authenticated user.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Credit card summaries returned successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @GetMapping("/credit-cards")
    public List<CreditCardInvoiceSummaryResponse> getCreditCardSummaries() {
        return dashboardService.getCreditCardSummaries();
    }

    @GetMapping("/credit-cards/trend")
    @Operation(
            summary = "Get annual credit card expense trend",
            description = """
                Returns the monthly evolution of credit card invoices for the selected year.
                The response includes the monthly total and the invoice amount grouped by credit card.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Credit card trend retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public ResponseEntity<List<MonthlyCreditCardTrendResponse>> getCreditCardTrend(
            @RequestParam
            @Parameter(
                    description = "Year to generate the trend",
                    example = "2026"
            )
            Integer year
    ) {

        return ResponseEntity.ok(
                dashboardService.getCreditCardTrend(year)
        );
    }

    @Operation(
            summary = "Get household cash flow",
            description = """
            Returns the monthly cash flow for the authenticated user's household.

            Includes:
            - Monthly income
            - Monthly expenses
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cash flow retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @GetMapping("/cash-flow")
    public ResponseEntity<List<CashFlowResponse>> getCashFlow(

            @RequestParam
            @Parameter(
                    description = "Year used to generate the cash flow",
                    example = "2026"
            )
            Integer year

    ) {

        return ResponseEntity.ok(
                dashboardService.getCashFlow(year)
        );
    }

    @Operation(
            summary = "Get cumulative financial result",
            description = """
            Returns the cumulative financial result
            for the authenticated user's household.

            Includes:
            - Monthly income
            - Monthly expenses
            - Monthly result
            - Cumulative result
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cumulative result retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @GetMapping("/cumulative-result")
    public ResponseEntity<List<CumulativeResultResponse>> getCumulativeResult(

            @RequestParam
            @Parameter(
                    description = "Year used to generate the cumulative result",
                    example = "2026"
            )
            Integer year

    ) {

        return ResponseEntity.ok(
                dashboardService.getCumulativeResult(year)
        );
    }

    @Operation(
            summary = "Retrieve individual cumulative financial result",
            description = """
                Returns the cumulative financial result
                for the authenticated user.

                Includes:
                - Monthly income
                - Monthly expenses
                - Monthly result
                - Cumulative result
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Individual cumulative result retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @GetMapping("/cumulative-result/me")
    public ResponseEntity<List<CumulativeResultResponse>> getMyCumulativeResult(

            @RequestParam
            @Parameter(
                    description = "Year used to generate the individual cumulative result",
                    example = "2026"
            )
            Integer year

    ) {

        return ResponseEntity.ok(
                dashboardService.getMyCumulativeResult(year)
        );
    }

    @Operation(
            summary = "Get household income commitment",
            description = """
            Returns the current household income commitment.

            Includes:
            - Monthly recurring income
            - Monthly recurring expenses
            - Unpaid credit card installments
            - Total monthly commitments
            - Available monthly income
            - Commitment percentage
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Income commitment retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @GetMapping("/income-commitment")
    public ResponseEntity<IncomeCommitmentResponse> getIncomeCommitment() {

        return ResponseEntity.ok(
                dashboardService.getIncomeCommitment()
        );
    }
}
