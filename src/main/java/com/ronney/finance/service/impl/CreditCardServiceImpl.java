package com.ronney.finance.service.impl;

import com.ronney.finance.domain.entity.CreditCard;
import com.ronney.finance.domain.entity.User;
import com.ronney.finance.dto.request.CreditCardRequest;
import com.ronney.finance.dto.response.CreditCardResponse;
import com.ronney.finance.exception.ResourceNotFoundException;
import com.ronney.finance.repository.CreditCardRepository;
import com.ronney.finance.service.CreditCardService;
import com.ronney.finance.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditCardServiceImpl implements CreditCardService {
    private final CreditCardRepository creditCardRepository;
    private final CurrentUserService currentUserService;

    @Override
    public CreditCardResponse create(CreditCardRequest request) {
        User user = currentUserService.getAuthenticatedUser();

        CreditCard creditCard = CreditCard.builder()
                .id(UUID.randomUUID())
                .name(request.name())
                .creditLimit(request.creditLimit())
                .closingDay(request.closingDay())
                .dueDay(request.dueDay())
                .user(user)
                .build();

        creditCard = creditCardRepository.save(creditCard);

        return toResponse(creditCard);
    }

    @Override
    public List<CreditCardResponse> findAll() {
        User user = currentUserService.getAuthenticatedUser();

        return creditCardRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CreditCardResponse findById(
            UUID id
    ) {
        User user = currentUserService.getAuthenticatedUser();

        CreditCard creditCard = creditCardRepository
                        .findByIdAndUserId(
                                id,
                                user.getId()
                        )
                        .orElseThrow(() -> new ResourceNotFoundException("Credit card not found."));

        return toResponse(creditCard);
    }

    @Override
    public CreditCardResponse update(
            UUID id,
            CreditCardRequest request
    ) {
        User user = currentUserService.getAuthenticatedUser();

        CreditCard creditCard =creditCardRepository
                        .findByIdAndUserId(
                                id,
                                user.getId()
                        )
                        .orElseThrow(() -> new ResourceNotFoundException("Credit card not found."));

        creditCard.setName(
                request.name()
        );

        creditCard.setCreditLimit(
                request.creditLimit()
        );

        creditCard.setClosingDay(
                request.closingDay()
        );

        creditCard.setDueDay(
                request.dueDay()
        );

        creditCard = creditCardRepository.save(creditCard);

        return toResponse(creditCard);
    }

    @Override
    public void delete(
            UUID id
    ) {
        User user = currentUserService.getAuthenticatedUser();

        CreditCard creditCard = creditCardRepository
                        .findByIdAndUserId(
                                id,
                                user.getId()
                        )
                        .orElseThrow(() -> new ResourceNotFoundException("Credit card not found."));

        creditCardRepository.delete(creditCard);
    }

    private CreditCardResponse toResponse(
            CreditCard creditCard
    ) {
        return new CreditCardResponse(
                creditCard.getId(),
                creditCard.getName(),
                creditCard.getCreditLimit(),
                creditCard.getClosingDay(),
                creditCard.getDueDay()
        );
    }
}
