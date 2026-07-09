package com.ronney.finance.repository;

import com.ronney.finance.domain.entity.FinancialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

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
}