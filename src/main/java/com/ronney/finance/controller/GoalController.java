package com.ronney.finance.controller;

import com.ronney.finance.dto.request.GoalRequest;
import com.ronney.finance.dto.response.GoalResponse;
import com.ronney.finance.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Financial Goals",
        description = "Manage personal financial goals."
)
@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
public class GoalController {
    private final GoalService goalService;

    @Operation(
            summary = "Create financial goal",
            description = "Creates a new financial goal for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Goal created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GoalResponse create(
            @Valid
            @RequestBody
            GoalRequest request
    ) {
        return goalService.create(request);
    }

    @Operation(
            summary = "List financial goals",
            description = "Returns all financial goals for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Goals returned successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found"
            )
    })
    @GetMapping
    public List<GoalResponse> findAll() {
        return goalService.findAll();
    }

    @Operation(
            summary = "Find financial goal",
            description = "Returns a financial goal by its identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Goal returned successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found"
            )
    })
    @GetMapping("/{id}")
    public GoalResponse findById(
            @PathVariable UUID id
    ) {
        return goalService.findById(id);
    }

    @Operation(
            summary = "Update financial goal",
            description = "Updates an existing financial goal."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Goal updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Goal not found"
            )
    })
    @PutMapping("/{id}")
    public GoalResponse update(
            @PathVariable UUID id,
            @Valid
            @RequestBody
            GoalRequest request
    ) {
        return goalService.update(id, request);
    }

    @Operation(
            summary = "Delete financial goal",
            description = "Deletes a financial goal."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Goal deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Goal not found"
            )
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(
                    description = "Goal identifier",
                    example = "4d0df1d8-8b62-4c0e-bef8-7dbfb74b27f6"
            )
            @PathVariable UUID id
    ) {
        goalService.delete(id);
    }
}
