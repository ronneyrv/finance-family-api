package com.ronney.finance.dto.response;

import com.ronney.finance.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String description,
        BigDecimal amount,
        LocalDate transactionDate,
        TransactionType type,
        UUID categoryId,
        String category,
        UUID subCategoryId,
        String subCategory
) {
}
