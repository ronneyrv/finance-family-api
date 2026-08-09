package com.ronney.finance.repository;

import com.ronney.finance.domain.entity.FinancialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialAccountRepository
        extends JpaRepository<FinancialAccount, UUID> {

    List<FinancialAccount> findByUserId(
            UUID userId
    );

    Optional<FinancialAccount> findByIdAndUserId(
            UUID id,
            UUID userId
    );

    @Query("""
    SELECT COALESCE(
        SUM(
            fa.initialBalance
            + COALESCE(
                (
                    SELECT SUM(
                        CASE
                            WHEN t.type = 'INCOME'
                            THEN t.amount
                            ELSE -t.amount
                        END
                    )
                    FROM Transaction t
                    WHERE t.financialAccount.id = fa.id
                ),
                0
            )
        ),
        0
    )
    FROM FinancialAccount fa
    WHERE fa.user.household.id = :householdId
    """)
    BigDecimal sumCurrentBalanceByHousehold(
            @Param("householdId") UUID householdId
    );
}