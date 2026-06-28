package com.ronney.finance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceInstallmentResponse(
        String description,
        String installment,
        BigDecimal amount,
        Boolean paid,
        LocalDate paidAt
) {
}
