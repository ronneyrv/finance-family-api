package com.ronney.finance.repository;

import com.ronney.finance.domain.entity.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringTransactionRepository
        extends JpaRepository<RecurringTransaction, UUID> {

    List<RecurringTransaction> findByUserIdOrderByDayOfMonthAsc(
            UUID userId
    );

    List<RecurringTransaction> findByUserIdAndActiveTrueOrderByDayOfMonthAsc(
            UUID userId
    );

    Optional<RecurringTransaction> findByIdAndUserId(
            UUID id,
            UUID userId
    );

    @Query("""
        SELECT r
        FROM RecurringTransaction r
        WHERE r.user.id = :userId
        AND r.active = true
        AND r.startDate <= :periodEnd
        AND (
            r.endDate IS NULL
            OR r.endDate >= :periodStart
        )
        ORDER BY r.dayOfMonth ASC
        """)
    List<RecurringTransaction> findActiveForPeriod(
            @Param("userId") UUID userId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd
    );
}