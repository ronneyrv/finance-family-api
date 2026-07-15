package com.ronney.finance.repository;

import com.ronney.finance.domain.entity.Transaction;
import com.ronney.finance.domain.enums.PaymentMethod;
import com.ronney.finance.domain.enums.TransactionKind;
import com.ronney.finance.domain.enums.TransactionType;
import com.ronney.finance.repository.projection.MonthlySummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Page<Transaction> findByUserId(
            UUID id,
            Pageable pageable
    );

    Optional<Transaction> findByIdAndUserId(
            UUID id,
            UUID userId
    );

    Page<Transaction> findByUserIdAndTransactionDateBetween(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.user.id = :userId
    AND t.type = :type
    AND t.transactionKind = :transactionKind
    """)
    BigDecimal sumByUserIdAndType(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("transactionKind") TransactionKind transactionKind
    );

    @Query("""
        SELECT
            c.name as category,
            COALESCE(SUM(t.amount),0) as amount
        FROM Transaction t
        JOIN t.category c
        WHERE t.user.id = :userId
        AND t.type = 'EXPENSE'
        AND t.transactionKind = 'REGULAR'
        GROUP BY c.name
        ORDER BY amount DESC
        """)
    List<CategoryExpenseProjection> findExpensesByCategory(
            @Param("userId") UUID userId
    );

    @Query("""
        SELECT
            MONTH(t.transactionDate) as month,
            COALESCE(
                SUM(
                    CASE
                        WHEN t.type = 'INCOME'
                        THEN t.amount
                        ELSE 0
                    END
                ),
                0
            ) as income,
            COALESCE(
                SUM(
                    CASE
                        WHEN t.type = 'EXPENSE'
                        THEN t.amount
                        ELSE 0
                    END
                ),
                0
            ) as expense
        FROM Transaction t
        WHERE t.user.id = :userId
        AND YEAR(t.transactionDate) = :year
        AND t.transactionKind = 'REGULAR'
        GROUP BY MONTH(t.transactionDate)
        ORDER BY MONTH(t.transactionDate)
        """)
    List<MonthlySummaryProjection> findMonthlySummary(
            @Param("userId") UUID userId,
            @Param("year") Integer year
    );

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.user.id = :userId
        AND t.type = :type
        AND t.paymentMethod IN :paymentMethods
        """)
    BigDecimal sumByUserIdAndTypeAndPaymentMethods(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("paymentMethods")
            List<PaymentMethod> paymentMethods
    );

    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.financialAccount.id = :accountId
    AND t.type = :type
    """)
    BigDecimal sumByFinancialAccountIdAndType(
            @Param("accountId") UUID accountId,
            @Param("type") TransactionType type
    );

    @Query("""
    SELECT MIN(t.transactionDate)
    FROM Transaction t
    WHERE t.user.id = :userId
    """)
    Optional<LocalDate> findFirstTransactionDate(
            @Param("userId") UUID userId
    );
}
