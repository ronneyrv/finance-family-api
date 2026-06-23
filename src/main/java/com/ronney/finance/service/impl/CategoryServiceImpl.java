package com.ronney.finance.service.impl;

import com.ronney.finance.domain.entity.Category;
import com.ronney.finance.domain.enums.TransactionType;
import com.ronney.finance.dto.response.CategoryResponse;
import com.ronney.finance.dto.response.SubCategoryResponse;
import com.ronney.finance.repository.CategoryRepository;
import com.ronney.finance.repository.SubCategoryRepository;
import com.ronney.finance.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    @Override
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<CategoryResponse> findByType(
            TransactionType type
    ) {
        return categoryRepository.findByType(type)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<SubCategoryResponse> findSubCategories(UUID categoryId) {
        return subCategoryRepository
                .findByCategoryId(categoryId)
                .stream()
                .map(sub -> new SubCategoryResponse(
                        sub.getId(),
                        sub.getName()
                    )
                )
                .toList();
    }

    private CategoryResponse toResponse(
            Category category
    ) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType()
        );
    }
}
