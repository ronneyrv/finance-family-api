package com.ronney.finance.repository;

import com.ronney.finance.domain.entity.CreditCardInstallment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CreditCardInstallmentRepository
        extends JpaRepository<CreditCardInstallment, UUID> {

    List<CreditCardInstallment> findByPurchaseCreditCardId(
            UUID creditCardId
    );

    List<CreditCardInstallment>
    findByPurchaseCreditCardIdAndInvoiceMonthAndInvoiceYear(
            UUID creditCardId,
            Integer invoiceMonth,
            Integer invoiceYear
    );

    List<CreditCardInstallment> findByPurchaseCreditCardIdAndPaidFalse(
            UUID creditCardId
    );
}