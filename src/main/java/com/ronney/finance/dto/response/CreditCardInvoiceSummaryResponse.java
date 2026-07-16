package com.ronney.finance.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditCardInvoiceSummaryResponse(
        UUID creditCardId,
        String cardName,
        BigDecimal invoiceAmount,
        Integer installmentCount,
        Integer dueDay,
        Boolean hasOpenInvoice
) {
}