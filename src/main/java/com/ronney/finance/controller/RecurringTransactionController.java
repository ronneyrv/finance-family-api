package com.ronney.finance.controller;

import com.ronney.finance.dto.request.RecurringTransactionRequest;
import com.ronney.finance.dto.response.RecurringTransactionResponse;
import com.ronney.finance.service.RecurringTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recurring-transactions")
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecurringTransactionResponse create(
            @RequestBody @Valid
            RecurringTransactionRequest request
    ) {
        return recurringTransactionService.create(request);
    }

    @GetMapping
    public List<RecurringTransactionResponse> findAll() {
        return recurringTransactionService.findAll();
    }

    @GetMapping("/{id}")
    public RecurringTransactionResponse findById(
            @PathVariable UUID id
    ) {
        return recurringTransactionService.findById(id);
    }

    @PutMapping("/{id}")
    public RecurringTransactionResponse update(
            @PathVariable UUID id,
            @RequestBody @Valid
            RecurringTransactionRequest request
    ) {
        return recurringTransactionService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public RecurringTransactionResponse updateStatus(
            @PathVariable UUID id,
            @RequestParam boolean active
    ) {
        return recurringTransactionService.updateStatus(
                id,
                active
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id
    ) {
        recurringTransactionService.delete(id);
    }
}