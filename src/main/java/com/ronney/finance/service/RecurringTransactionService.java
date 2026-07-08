package com.ronney.finance.service;

import com.ronney.finance.dto.request.RecurringTransactionRequest;
import com.ronney.finance.dto.response.RecurringTransactionResponse;

import java.util.List;
import java.util.UUID;

public interface RecurringTransactionService {

    RecurringTransactionResponse create(
            RecurringTransactionRequest request
    );

    List<RecurringTransactionResponse> findAll();

    RecurringTransactionResponse findById(
            UUID id
    );

    RecurringTransactionResponse update(
            UUID id,
            RecurringTransactionRequest request
    );

    RecurringTransactionResponse updateStatus(
            UUID id,
            boolean active
    );

    void delete(
            UUID id
    );
}