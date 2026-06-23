package com.ronney.finance.controller;

import com.ronney.finance.domain.enums.TransactionType;
import com.ronney.finance.dto.response.CategoryResponse;
import com.ronney.finance.dto.response.SubCategoryResponse;
import com.ronney.finance.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponse> findAll(
            @RequestParam(required = false)
            TransactionType type
    ) {
        if (type!= null) {
            return categoryService.findByType(type);
        }
        return categoryService.findAll();
    }

    @GetMapping("/{id}/sub-categories")
    public List<SubCategoryResponse> findSubCategories(
            @PathVariable UUID id
    ) {
       return  categoryService.findSubCategories(id);
    }
}
