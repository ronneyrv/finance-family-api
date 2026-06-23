package com.ronney.finance.controller;

import com.ronney.finance.dto.request.TransactionRequest;
import com.ronney.finance.dto.response.TransactionResponse;
import com.ronney.finance.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(
            @RequestBody @Valid
            TransactionRequest request
    ) {
        return transactionService.create(request);
    }
}