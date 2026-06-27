package com.ronney.finance.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record InstallmentResponse(
        UUID id,
        String description,
        Integer installmentNumber,
        Integer totalInstallments,
        BigDecimal amount,
        Integer invoiceMonth,
        Integer invoiceYear,
        Boolean paid
) {
}
