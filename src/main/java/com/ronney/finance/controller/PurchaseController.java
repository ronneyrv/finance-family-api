package com.ronney.finance.controller;

import com.ronney.finance.dto.request.InvoicePaymentRequest;
import com.ronney.finance.dto.request.PurchaseRequest;
import com.ronney.finance.dto.response.InstallmentResponse;
import com.ronney.finance.dto.response.InvoiceResponse;
import com.ronney.finance.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Purchases & Invoices",
        description = "Manage installment purchases and monthly credit card invoices."
)
@RestController
@RequestMapping("/api/v1/credit-cards")
@RequiredArgsConstructor
public class PurchaseController {
    private final PurchaseService purchaseService;

    @Operation(
            summary = "Create installment purchase",
            description = """
                Creates a new installment purchase and automatically
                generates the monthly installments according to the
                credit card closing day.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Purchase created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Credit card not found"
            )
    })
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

    @Operation(
            summary = "Get monthly invoice",
            description = """
                Returns a monthly invoice for a credit card.

                Includes:
                - invoice total
                - available credit
                - installments
                - payment status
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Invoice returned successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Invoice not found"
            )
    })
    @GetMapping("/{id}/invoice")
    public InvoiceResponse getInvoice(
            @PathVariable UUID id,

            @Parameter(
                    description = "Invoice month",
                    example = "10"
            )
            @RequestParam Integer month,

            @Parameter(
                    description = "Invoice year",
                    example = "2026"
            )
            @RequestParam Integer year
    ) {
        return purchaseService.getInvoice(
                id,
                month,
                year
        );
    }

    @Operation(
            summary = "Pay invoice",
            description = """
                Marks all installments from the selected invoice
                as paid and records the payment date.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Invoice paid successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Invoice not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Invoice already paid"
            )
    })
    @PostMapping("/{id}/invoice/pay")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void payInvoice(
            @PathVariable UUID id,

            @Parameter(
                    description = "Invoice month",
                    example = "10"
            )
            @RequestParam Integer month,

            @Parameter(
                    description = "Invoice year",
                    example = "2026"
            )
            @RequestParam Integer year,

            @Valid
            @RequestBody InvoicePaymentRequest request
    ) {
        purchaseService.payInvoice(
                id,
                month,
                year,
                request
        );
    }
}

