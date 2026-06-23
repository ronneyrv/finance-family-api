package com.ronney.finance.service;

import com.ronney.finance.dto.request.TransactionRequest;
import com.ronney.finance.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface TransactionService {
    TransactionResponse create(
            TransactionRequest request
    );

    Page<TransactionResponse> findAll(
            Pageable pageable,
            LocalDate startDate,
            LocalDate endDate
    );

    TransactionResponse findById(
            UUID id
    );

    TransactionResponse update(
            UUID id,
            TransactionRequest request
    );

    void delete(
            UUID id
    );
}
