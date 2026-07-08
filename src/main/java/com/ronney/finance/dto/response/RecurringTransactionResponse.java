package com.ronney.finance.dto.response;

import com.ronney.finance.domain.enums.PaymentMethod;
import com.ronney.finance.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecurringTransactionResponse(
        UUID id,
        String description,
        BigDecimal amount,
        TransactionType type,
        PaymentMethod paymentMethod,
        Integer dayOfMonth,
        LocalDate startDate,
        LocalDate endDate,
        Boolean active,
        UUID categoryId,
        String category,
        UUID subCategoryId,
        String subCategory
) {
}