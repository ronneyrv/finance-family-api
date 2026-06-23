package com.ronney.finance.dto.response;

import com.ronney.finance.domain.enums.TransactionType;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        TransactionType type
) {
}
