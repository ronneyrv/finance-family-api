package com.ronney.finance.repository;

import com.ronney.finance.domain.entity.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardRepository extends JpaRepository<CreditCard, UUID> {

    List<CreditCard> findByUserId(
            UUID userId
    );

    Optional<CreditCard> findByIdAndUserId(
            UUID id,
            UUID userId
    );
}
