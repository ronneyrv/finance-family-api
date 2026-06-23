package com.ronney.finance.repository;

import com.ronney.finance.domain.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
}
