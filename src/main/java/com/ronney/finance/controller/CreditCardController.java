package com.ronney.finance.controller;

import com.ronney.finance.dto.request.CreditCardRequest;
import com.ronney.finance.dto.response.CreditCardResponse;
import com.ronney.finance.service.CreditCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/credit-cards")
@RequiredArgsConstructor
public class CreditCardController {
    private final CreditCardService creditCardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreditCardResponse create(
            @Valid
            @RequestBody
            CreditCardRequest request
    ) {
        return creditCardService.create(request);
    }

    @GetMapping
    public List<CreditCardResponse> findAll() {
        return creditCardService.findAll();
    }

    @GetMapping("/{id}")
    public CreditCardResponse findById(
            @PathVariable UUID id
    ) {
        return creditCardService.findById(id);
    }

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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id
    ) {
        creditCardService.delete(id);
    }
}
