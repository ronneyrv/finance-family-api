package com.ronney.finance.controller;

import com.ronney.finance.dto.request.TransactionRequest;
import com.ronney.finance.dto.response.TransactionResponse;
import com.ronney.finance.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@Tag(
        name = "Transactions",
        description = "Manage financial transactions."
)
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(
            summary = "Create transaction",
            description = "Creates a new financial transaction."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Transaction created successfully"
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
    public TransactionResponse create(
            @RequestBody @Valid
            TransactionRequest request
    ) {
        return transactionService.create(request);
    }

    @Operation(
            summary = "List transactions",
            description = "Returns a paginated list of user transactions."
    )
    @GetMapping
    public Page<TransactionResponse> findAll(
            @PageableDefault(
                    size = 20,
                    sort = "transactionDate",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,

            @RequestParam(required = false)
            LocalDate startDate,

            @RequestParam(required = false)
            LocalDate endDate
    ) {
        return transactionService.findAll(pageable, startDate, endDate);
    }

    @Operation(
            summary = "Find transaction",
            description = "Returns a transaction by its identifier."
    )
    @GetMapping("/{id}")
    public TransactionResponse findById(
            @PathVariable UUID id
    ) {
        return transactionService.findById(id);
    }

    @Operation(
            summary = "Update transaction",
            description = "Updates an existing transaction."
    )
    @PutMapping("/{id}")
    public TransactionResponse update(
            @PathVariable UUID id,
            @RequestBody @Valid
            TransactionRequest request
    ) {
        return transactionService.update(id, request);
    }

    @Operation(
            summary = "Delete transaction",
            description = "Deletes a transaction."
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id
    ) {
        transactionService.delete(id);
    }
}