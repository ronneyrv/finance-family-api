package com.ronney.finance.service;

import com.ronney.finance.dto.request.PurchaseRequest;
import com.ronney.finance.dto.response.InstallmentResponse;
import com.ronney.finance.dto.response.InvoiceResponse;

import java.util.List;
import java.util.UUID;

public interface PurchaseService {
    List<InstallmentResponse> createPurchase(
            UUID creditCardId,
            PurchaseRequest request
    );

    InvoiceResponse getInvoice(
            UUID creditCardId,
            Integer month,
            Integer year
    );

    void payInvoice(
            UUID creditCardId,
            Integer month,
            Integer year
    );
}
