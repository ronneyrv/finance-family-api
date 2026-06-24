package com.ronney.finance.repository;

import com.ronney.finance.domain.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<Goal, UUID> {
    List<Goal> findByUserId(
            UUID userId
    );

    Optional<Goal> findByIdAndUserId(
            UUID id,
            UUID userId
    );
}
