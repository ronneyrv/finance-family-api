package com.ronney.finance.repository;

import com.ronney.finance.domain.entity.Category;
import com.ronney.finance.domain.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByType(TransactionType type);

    Optional<Category> findByName(String name);

    boolean existsByName(String name);
}
