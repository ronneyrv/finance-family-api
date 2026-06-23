package com.ronney.finance.controller;

import com.ronney.finance.dto.request.TransactionRequest;
import com.ronney.finance.dto.response.TransactionResponse;
import com.ronney.finance.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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

}