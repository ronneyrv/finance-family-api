package com.ronney.finance.repository;

import com.ronney.finance.domain.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {

    Optional<Purchase> findByIdAndCreditCardUserId(
            UUID id,
            UUID userId
    );

    List<Purchase> findDistinctByCreditCardUserIdAndInstallmentsPaidFalseOrderByPurchaseDateDesc(
            UUID userId
    );

    @Query("""
    SELECT
        c.name as category,
        COALESCE(SUM(p.totalAmount), 0) as amount
    FROM Purchase p
    JOIN p.category c
    WHERE p.creditCard.user.id = :userId
    AND YEAR(p.purchaseDate) = :year
    GROUP BY c.name
    ORDER BY amount DESC
    """)
    List<CategoryExpenseProjection> findExpensesByCategoryAndYear(
            @Param("userId") UUID userId,
            @Param("year") Integer year
    );
}