package com.ronney.finance.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditCardResponse(
        UUID id,
        String name,
        BigDecimal creditLimit,
        Integer closingDay,
        Integer dueDay
) {
}
