package com.ronney.finance.service;

import com.ronney.finance.dto.request.FinancialAccountRequest;
import com.ronney.finance.dto.response.FinancialAccountResponse;

import java.util.List;
import java.util.UUID;

public interface FinancialAccountService {

    FinancialAccountResponse create(
            FinancialAccountRequest request
    );

    List<FinancialAccountResponse> findAll();

    FinancialAccountResponse findById(
            UUID id
    );

    FinancialAccountResponse update(
            UUID id,
            FinancialAccountRequest request
    );

    void delete(
            UUID id
    );
}