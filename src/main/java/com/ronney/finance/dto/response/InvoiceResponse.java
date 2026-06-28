package com.ronney.finance.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record InvoiceResponse(
        String card,
        Integer closingDay,
        Integer dueDay,
        Integer month,
        Integer year,
        BigDecimal total,
        BigDecimal availableLimit,
        List<InvoiceInstallmentResponse> installments
) {
}
