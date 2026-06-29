package com.ronney.finance.controller;

import com.ronney.finance.domain.enums.TransactionType;
import com.ronney.finance.dto.response.CategoryResponse;
import com.ronney.finance.dto.response.SubCategoryResponse;
import com.ronney.finance.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Categories",
        description = "Manage transaction categories and subcategories."
)
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(
            summary = "List categories",
            description = "Returns all categories. Optionally filters by transaction type."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categories returned successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @GetMapping
    public List<CategoryResponse> findAll(
            @Parameter(
                    description = "Filter categories by transaction type",
                    example = "INCOME"
            )
            @RequestParam(required = false)
            TransactionType type
    ) {
        if (type!= null) {
            return categoryService.findByType(type);
        }
        return categoryService.findAll();
    }

    @Operation(
            summary = "List subcategories",
            description = "Returns all subcategories belonging to a category."
    )
    @GetMapping("/{id}/sub-categories")
    public List<SubCategoryResponse> findSubCategories(
            @PathVariable UUID id
    ) {
       return  categoryService.findSubCategories(id);
    }
}
