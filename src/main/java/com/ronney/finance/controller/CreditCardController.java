package com.ronney.finance.controller;

import com.ronney.finance.dto.request.CreditCardRequest;
import com.ronney.finance.dto.response.CreditCardResponse;
import com.ronney.finance.service.CreditCardService;
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
        name = "Credit Cards",
        description = "Manage credit cards and available limits."
)
@RestController
@RequestMapping("/api/v1/credit-cards")
@RequiredArgsConstructor
public class CreditCardController {
    private final CreditCardService creditCardService;

    @Operation(
            summary = "Create credit card",
            description = "Creates a new credit card for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Credit card created successfully"
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
    public CreditCardResponse create(
            @Valid
            @RequestBody
            CreditCardRequest request
    ) {
        return creditCardService.create(request);
    }

    @Operation(
            summary = "List credit cards",
            description = "Returns all credit cards of the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Credit Cards returned successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Credit Cards not found"
            )
    })
    @GetMapping
    public List<CreditCardResponse> findAll() {
        return creditCardService.findAll();
    }

    @Operation(
            summary = "Find credit card",
            description = "Returns a credit card by its identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Credit Card returned successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Credit Card not found"
            )
    })
    @GetMapping("/{id}")
    public CreditCardResponse findById(
            @PathVariable UUID id
    ) {
        return creditCardService.findById(id);
    }

    @Operation(
            summary = "Update credit card",
            description = "Updates an existing credit card."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Credit card updated successfully"
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
                    description = "Credit card not found"
            )
    })
    @PutMapping("/{id}")
    public CreditCardResponse update(
            @PathVariable UUID id,
            @Valid
            @RequestBody
            CreditCardRequest request
    ) {
        return creditCardService.update(
                id,
                request
        );
    }

    @Operation(
            summary = "Delete credit card",
            description = "Deletes a credit card."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Credit card deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Credit card not found"
            )
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(
                    description = "Credit card identifier",
                    example = "4d0df1d8-8b62-4c0e-bef8-7dbfb74b27f6"
            )
            @PathVariable UUID id
    ) {
        creditCardService.delete(id);
    }
}
