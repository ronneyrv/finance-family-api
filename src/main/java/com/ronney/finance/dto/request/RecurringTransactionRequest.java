package com.ronney.finance.dto.request;

import com.ronney.finance.domain.enums.PaymentMethod;
import com.ronney.finance.domain.enums.TransactionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecurringTransactionRequest(

        @NotBlank
        String description,

        @NotNull
        @Positive
        BigDecimal amount,

        @NotNull
        TransactionType type,

        @NotNull
        PaymentMethod paymentMethod,

        @NotNull
        @Min(1)
        @Max(31)
        Integer dayOfMonth,

        @NotNull
        LocalDate startDate,

        LocalDate endDate,

        @NotNull
        UUID categoryId,

        UUID subCategoryId
) {
}