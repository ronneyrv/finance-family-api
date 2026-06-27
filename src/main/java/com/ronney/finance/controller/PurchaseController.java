package com.ronney.finance.controller;

import com.ronney.finance.dto.request.PurchaseRequest;
import com.ronney.finance.dto.response.InstallmentResponse;
import com.ronney.finance.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/credit-cards")
@RequiredArgsConstructor
public class PurchaseController {
    private final PurchaseService purchaseService;

    @PostMapping("/{id}/purchases")
    @ResponseStatus(HttpStatus.CREATED)
    public List<InstallmentResponse> createPurchase(
            @PathVariable UUID id,
            @Valid @RequestBody PurchaseRequest request
    ) {
        return purchaseService.createPurchase(
                id,
                request
        );
    }
}

