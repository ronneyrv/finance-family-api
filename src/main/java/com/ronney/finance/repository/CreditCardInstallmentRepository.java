package com.ronney.finance.repository;

import com.ronney.finance.domain.entity.CreditCardInstallment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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

    List<CreditCardInstallment>
    findByPurchaseCreditCardUserIdAndInvoiceMonthAndInvoiceYear(
            UUID userId,
            Integer invoiceMonth,
            Integer invoiceYear
    );

    @EntityGraph(attributePaths = {
            "purchase",
            "purchase.creditCard"
    })
    List<CreditCardInstallment> findByPurchaseCreditCardUserIdAndInvoiceYear(
            UUID userId,
            Integer invoiceYear
    );

    @Query("""
    SELECT COALESCE(SUM(i.amount), 0)
    FROM CreditCardInstallment i
    WHERE i.purchase.creditCard.user.household.id = :householdId
    AND i.paid = false
    """)
    BigDecimal sumUnpaidInstallmentsByHousehold(
            @Param("householdId") UUID householdId
    );

    @EntityGraph(attributePaths = {
            "purchase",
            "purchase.creditCard"
    })
    @Query("""
    SELECT i
    FROM CreditCardInstallment i
    WHERE i.purchase.creditCard.user.household.id = :householdId
    AND i.invoiceMonth = :invoiceMonth
    AND i.invoiceYear = :invoiceYear
    """)
    List<CreditCardInstallment> findByHouseholdInvoice(
            @Param("householdId") UUID householdId,
            @Param("invoiceMonth") Integer invoiceMonth,
            @Param("invoiceYear") Integer invoiceYear
    );

    @Query("""
    SELECT
        c.name as category,
        COALESCE(SUM(i.amount), 0) as amount
    FROM CreditCardInstallment i
    JOIN i.purchase p
    JOIN p.category c
    WHERE p.creditCard.user.id = :userId
    AND i.invoiceMonth = :month
    AND i.invoiceYear = :year
    GROUP BY c.name
    ORDER BY amount DESC
    """)
    List<CategoryExpenseProjection> findMonthlyExpensesByCategory(
            @Param("userId") UUID userId,
            @Param("month") Integer month,
            @Param("year") Integer year
    );
}