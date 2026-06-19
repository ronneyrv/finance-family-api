package com.ronney.finance.repository;

import com.ronney.finance.domain.entity.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HouseholdRepository extends JpaRepository<Household, UUID> {
}
