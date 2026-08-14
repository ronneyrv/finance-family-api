package com.ronney.finance.repository;

import com.ronney.finance.domain.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

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
}