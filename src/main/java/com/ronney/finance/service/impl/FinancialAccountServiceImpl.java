package com.ronney.finance.service.impl;

import com.ronney.finance.domain.entity.FinancialAccount;
import com.ronney.finance.domain.entity.User;
import com.ronney.finance.dto.request.FinancialAccountRequest;
import com.ronney.finance.dto.response.FinancialAccountResponse;
import com.ronney.finance.exception.ResourceNotFoundException;
import com.ronney.finance.repository.FinancialAccountRepository;
import com.ronney.finance.service.CurrentUserService;
import com.ronney.finance.service.FinancialAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinancialAccountServiceImpl
        implements FinancialAccountService {

    private final FinancialAccountRepository financialAccountRepository;
    private final CurrentUserService currentUserService;

    @Override
    public FinancialAccountResponse create(
            FinancialAccountRequest request
    ) {
        User user = currentUserService.getAuthenticatedUser();

        FinancialAccount financialAccount = FinancialAccount.builder()
                .id(UUID.randomUUID())
                .name(request.name())
                .accountType(request.accountType())
                .initialBalance(request.initialBalance())
                .user(user)
                .build();

        financialAccount =
                financialAccountRepository.save(financialAccount);

        return toResponse(financialAccount);
    }

    @Override
    public List<FinancialAccountResponse> findAll() {
        User user = currentUserService.getAuthenticatedUser();

        return financialAccountRepository
                .findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public FinancialAccountResponse findById(
            UUID id
    ) {
        User user = currentUserService.getAuthenticatedUser();

        FinancialAccount financialAccount =
                findByIdAndUser(id, user);

        return toResponse(financialAccount);
    }

    @Override
    public FinancialAccountResponse update(
            UUID id,
            FinancialAccountRequest request
    ) {
        User user = currentUserService.getAuthenticatedUser();

        FinancialAccount financialAccount =
                findByIdAndUser(id, user);

        financialAccount.setName(request.name());
        financialAccount.setAccountType(request.accountType());
        financialAccount.setInitialBalance(request.initialBalance());

        financialAccount =
                financialAccountRepository.save(financialAccount);

        return toResponse(financialAccount);
    }

    @Override
    public void delete(
            UUID id
    ) {
        User user = currentUserService.getAuthenticatedUser();

        FinancialAccount financialAccount =
                findByIdAndUser(id, user);

        financialAccountRepository.delete(financialAccount);
    }

    private FinancialAccount findByIdAndUser(
            UUID id,
            User user
    ) {
        return financialAccountRepository
                .findByIdAndUserId(
                        id,
                        user.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Financial account not found."
                        )
                );
    }

    private FinancialAccountResponse toResponse(
            FinancialAccount financialAccount
    ) {
        return new FinancialAccountResponse(
                financialAccount.getId(),
                financialAccount.getName(),
                financialAccount.getAccountType(),
                financialAccount.getInitialBalance()
        );
    }
}