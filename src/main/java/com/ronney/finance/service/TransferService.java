package com.ronney.finance.service;

import com.ronney.finance.dto.request.TransferRequest;
import com.ronney.finance.dto.response.TransferResponse;

import java.util.UUID;

public interface TransferService {

    TransferResponse create(
            TransferRequest request
    );

    TransferResponse findById(
            UUID id
    );

    TransferResponse update(
            UUID id,
            TransferRequest request
    );

    void delete(
            UUID id
    );
}