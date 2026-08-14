package com.ronney.finance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PendingPurchaseResponse(
        UUID id,
        String description,
        LocalDate purchaseDate,
        BigDecimal totalAmount,
        Integer installmentCount,
        UUID creditCardId,
        String creditCardName,
        UUID categoryId,
        String categoryName,
        UUID subCategoryId,
        String subCategoryName
) {
}