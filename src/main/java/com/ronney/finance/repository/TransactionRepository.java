package com.ronney.finance.repository;

import com.ronney.finance.domain.entity.Transaction;
import com.ronney.finance.domain.enums.TransactionType;
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
        """)
    BigDecimal sumByUserIdAndType(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type
    );

    @Query("""
        SELECT
            c.name as category,
            COALESCE(SUM(t.amount),0) as amount
        FROM Transaction t
        JOIN t.category c
        WHERE t.user.id = :userId
        AND t.type = 'EXPENSE'
        GROUP BY c.name
        ORDER BY amount DESC
        """)
    List<CategoryExpenseProjection> findExpensesByCategory(
            @Param("userId") UUID userId
    );
}
