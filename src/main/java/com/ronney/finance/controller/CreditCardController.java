package com.ronney.finance.controller;

import com.ronney.finance.dto.request.CreditCardRequest;
import com.ronney.finance.dto.response.CreditCardResponse;
import com.ronney.finance.service.CreditCardService;
import io.swagger.v3.oas.annotations.Operation;
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
            @ApiResponse(responseCode = "201", description = "Credit card created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
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
    @GetMapping
    public List<CreditCardResponse> findAll() {
        return creditCardService.findAll();
    }

    @Operation(
            summary = "Find credit card",
            description = "Returns a credit card by its identifier."
    )
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
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id
    ) {
        creditCardService.delete(id);
    }
}
