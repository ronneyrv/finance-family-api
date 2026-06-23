package com.ronney.finance.repository;

import com.ronney.finance.domain.entity.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.UUID;

public interface SubCategoryRepository extends JpaRepository<SubCategory, UUID> {
    List<SubCategory> findByCategoryId(
            UUID categoryId
    );

    boolean existsByNameAndCategoryId(
            String name,
            UUID categoryId
    );

}
