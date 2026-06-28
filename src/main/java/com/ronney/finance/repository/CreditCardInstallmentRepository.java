package com.ronney.finance.repository;

import com.ronney.finance.domain.entity.CreditCardInstallment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CreditCardInstallmentRepository extends JpaRepository<CreditCardInstallment, UUID> {
    List<CreditCardInstallment> findByCreditCardId(
            UUID creditCardId
    );

    List<CreditCardInstallment> findByCreditCardIdAndInvoiceMonthAndInvoiceYear(
            UUID creditCardId,
            Integer invoiceMonth,
            Integer invoiceYear
    );

    List<CreditCardInstallment> findByCreditCardIdAndPaidFalse(
            UUID creditCardId
    );
}
