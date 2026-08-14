package com.ronney.finance.dto.request;

import java.util.UUID;

public record PurchaseCategoryRequest(
        UUID categoryId,
        UUID subCategoryId
) {
}