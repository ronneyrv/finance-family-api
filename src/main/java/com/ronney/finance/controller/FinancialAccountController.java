package com.ronney.finance.controller;

import com.ronney.finance.dto.request.FinancialAccountRequest;
import com.ronney.finance.dto.response.FinancialAccountResponse;
import com.ronney.finance.service.FinancialAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Financial Accounts",
        description = "Manage financial accounts and initial balances."
)
@RestController
@RequestMapping("/api/v1/financial-accounts")
@RequiredArgsConstructor
public class FinancialAccountController {

    private final FinancialAccountService financialAccountService;

    @Operation(
            summary = "Create financial account",
            description = "Creates a new financial account for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Financial account created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FinancialAccountResponse create(
            @Valid
            @RequestBody
            FinancialAccountRequest request
    ) {
        return financialAccountService.create(request);
    }

    @Operation(
            summary = "List financial accounts",
            description = "Returns all financial accounts of the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Financial accounts returned successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @GetMapping
    public List<FinancialAccountResponse> findAll() {
        return financialAccountService.findAll();
    }

    @Operation(
            summary = "Find financial account",
            description = "Returns a financial account by its identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Financial account returned successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Financial account not found"
            )
    })
    @GetMapping("/{id}")
    public FinancialAccountResponse findById(
            @Parameter(
                    description = "Financial account identifier"
            )
            @PathVariable UUID id
    ) {
        return financialAccountService.findById(id);
    }

    @Operation(
            summary = "Update financial account",
            description = "Updates an existing financial account."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Financial account updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Financial account not found"
            )
    })
    @PutMapping("/{id}")
    public FinancialAccountResponse update(
            @PathVariable UUID id,
            @Valid
            @RequestBody
            FinancialAccountRequest request
    ) {
        return financialAccountService.update(
                id,
                request
        );
    }

    @Operation(
            summary = "Delete financial account",
            description = "Deletes an existing financial account."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Financial account deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Financial account not found"
            )
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(
                    description = "Financial account identifier"
            )
            @PathVariable UUID id
    ) {
        financialAccountService.delete(id);
    }
}