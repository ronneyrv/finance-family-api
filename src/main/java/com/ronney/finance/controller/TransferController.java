package com.ronney.finance.controller;

import com.ronney.finance.dto.request.TransferRequest;
import com.ronney.finance.dto.response.TransferResponse;
import com.ronney.finance.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "Transfers",
        description = "Manage internal financial account transfers."
)
@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @Operation(
            summary = "Create transfer",
            description = "Creates an internal transfer between two financial accounts."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Transfer created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Financial account not found"
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse create(
            @Valid
            @RequestBody
            TransferRequest request
    ) {
        return transferService.create(request);
    }

    @Operation(
            summary = "Find transfer",
            description = "Returns an internal transfer by its identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transfer returned successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transfer not found"
            )
    })
    @GetMapping("/{id}")
    public TransferResponse findById(
            @Parameter(
                    description = "Transfer identifier"
            )
            @PathVariable UUID id
    ) {
        return transferService.findById(id);
    }

    @Operation(
            summary = "Update transfer",
            description = "Updates an existing internal transfer."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transfer updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transfer not found"
            )
    })
    @PutMapping("/{id}")
    public TransferResponse update(
            @PathVariable UUID id,
            @Valid
            @RequestBody
            TransferRequest request
    ) {
        return transferService.update(
                id,
                request
        );
    }

    @Operation(
            summary = "Delete transfer",
            description = "Deletes an internal transfer and both associated transactions."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Transfer deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transfer not found"
            )
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(
                    description = "Transfer identifier"
            )
            @PathVariable UUID id
    ) {
        transferService.delete(id);
    }
}