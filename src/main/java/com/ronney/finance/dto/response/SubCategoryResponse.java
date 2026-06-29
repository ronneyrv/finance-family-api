package com.ronney.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record SubCategoryResponse(
        @Schema(
                description = "Subcategory identifier"
        )
        UUID id,

        @Schema(
                description = "Subcategory name",
                example = "Salary"
        )
        String name
) {
}
