package com.ronney.finance.dto.request;

import com.ronney.finance.domain.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InvoicePaymentRequest(

        @Schema(
                description = "Financial account identifier used to pay the invoice",
                example = "4d0df1d8-8b62-4c0e-bef8-7dbfb74b27f6"
        )
        @NotNull
        UUID accountId,

        @Schema(
                description = "Payment method used to pay the invoice",
                example = "PIX"
        )
        @NotNull
        PaymentMethod paymentMethod
) {
}