package com.ronney.finance.service;

import com.ronney.finance.domain.enums.TransactionType;
import com.ronney.finance.dto.response.CategoryResponse;
import com.ronney.finance.dto.response.SubCategoryResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    List<CategoryResponse> findAll();

    List<CategoryResponse> findByType(
            TransactionType type
    );

    List<SubCategoryResponse> findSubCategories(
            UUID categoryId
    );
}
