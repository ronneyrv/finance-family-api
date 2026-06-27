package com.ronney.finance.service;

import com.ronney.finance.dto.request.CreditCardRequest;
import com.ronney.finance.dto.response.CreditCardResponse;

import java.util.List;
import java.util.UUID;

public interface CreditCardService {
    CreditCardResponse create(
            CreditCardRequest request
    );

    List<CreditCardResponse> findAll();

    CreditCardResponse findById(
            UUID id
    );

    CreditCardResponse update(
            UUID id,
            CreditCardRequest request
    );

    void delete(
            UUID id
    );
}
