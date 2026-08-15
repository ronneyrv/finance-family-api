package com.ronney.finance.service.impl;

import com.ronney.finance.domain.entity.FinancialAccount;
import com.ronney.finance.domain.entity.Transaction;
import com.ronney.finance.domain.entity.User;
import com.ronney.finance.domain.enums.TransactionKind;
import com.ronney.finance.domain.enums.TransactionType;
import com.ronney.finance.dto.request.TransferRequest;
import com.ronney.finance.dto.response.TransferResponse;
import com.ronney.finance.exception.ResourceNotFoundException;
import com.ronney.finance.repository.FinancialAccountRepository;
import com.ronney.finance.repository.TransactionRepository;
import com.ronney.finance.service.CurrentUserService;
import com.ronney.finance.service.TransferService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final TransactionRepository transactionRepository;
    private final FinancialAccountRepository financialAccountRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public TransferResponse create(TransferRequest request) {
        User user = currentUserService.getAuthenticatedUser();

        FinancialAccount sourceAccount = findAccount(
                request.sourceAccountId(),
                user
        );

        FinancialAccount destinationAccount = findAccount(
                request.destinationAccountId(),
                user
        );

        validateDifferentAccounts(
                sourceAccount,
                destinationAccount
        );

        UUID transferId = UUID.randomUUID();

        Transaction sourceTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .description(request.description())
                .amount(request.amount())
                .transactionDate(request.transactionDate())
                .type(TransactionType.EXPENSE)
                .transactionKind(TransactionKind.TRANSFER)
                .paymentMethod(null)
                .user(user)
                .category(null)
                .subCategory(null)
                .financialAccount(sourceAccount)
                .transferId(transferId)
                .build();

        Transaction destinationTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .description(request.description())
                .amount(request.amount())
                .transactionDate(request.transactionDate())
                .type(TransactionType.INCOME)
                .transactionKind(TransactionKind.TRANSFER)
                .paymentMethod(null)
                .user(user)
                .category(null)
                .subCategory(null)
                .financialAccount(destinationAccount)
                .transferId(transferId)
                .build();

        transactionRepository.save(sourceTransaction);
        transactionRepository.save(destinationTransaction);

        return toResponse(
                transferId,
                request,
                sourceAccount,
                destinationAccount
        );
    }

    @Override
    @Transactional
    public TransferResponse findById(UUID id) {
        User user = currentUserService.getAuthenticatedUser();

        List<Transaction> transactions =
                transactionRepository.findByTransferIdAndUserId(
                        id,
                        user.getId()
                );

        validateTransfer(transactions);

        Transaction sourceTransaction = findByType(
                transactions,
                TransactionType.EXPENSE
        );

        Transaction destinationTransaction = findByType(
                transactions,
                TransactionType.INCOME
        );

        return toResponse(
                id,
                sourceTransaction,
                destinationTransaction
        );
    }

    @Override
    @Transactional
    public TransferResponse update(
            UUID id,
            TransferRequest request
    ) {
        User user = currentUserService.getAuthenticatedUser();

        List<Transaction> transactions =
                transactionRepository.findByTransferIdAndUserId(
                        id,
                        user.getId()
                );

        validateTransfer(transactions);

        FinancialAccount sourceAccount = findAccount(
                request.sourceAccountId(),
                user
        );

        FinancialAccount destinationAccount = findAccount(
                request.destinationAccountId(),
                user
        );

        validateDifferentAccounts(
                sourceAccount,
                destinationAccount
        );

        Transaction sourceTransaction = findByType(
                transactions,
                TransactionType.EXPENSE
        );

        Transaction destinationTransaction = findByType(
                transactions,
                TransactionType.INCOME
        );

        updateTransaction(
                sourceTransaction,
                request,
                sourceAccount
        );

        updateTransaction(
                destinationTransaction,
                request,
                destinationAccount
        );

        transactionRepository.save(sourceTransaction);
        transactionRepository.save(destinationTransaction);

        return toResponse(
                id,
                request,
                sourceAccount,
                destinationAccount
        );
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = currentUserService.getAuthenticatedUser();

        List<Transaction> transactions =
                transactionRepository.findByTransferIdAndUserId(
                        id,
                        user.getId()
                );

        validateTransfer(transactions);

        transactionRepository.deleteAll(transactions);
    }

    private FinancialAccount findAccount(
            UUID accountId,
            User user
    ) {
        return financialAccountRepository
                .findByIdAndUserId(
                        accountId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Financial account not found."
                        )
                );
    }

    private void validateDifferentAccounts(
            FinancialAccount sourceAccount,
            FinancialAccount destinationAccount
    ) {
        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new IllegalArgumentException(
                    "Source and destination accounts must be different."
            );
        }
    }

    private void validateTransfer(
            List<Transaction> transactions
    ) {
        if (transactions.size() != 2) {
            throw new ResourceNotFoundException(
                    "Transfer not found."
            );
        }

        boolean validTransfer = transactions.stream()
                .allMatch(transaction ->
                        TransactionKind.TRANSFER.equals(
                                transaction.getTransactionKind()
                        )
                );

        if (!validTransfer) {
            throw new ResourceNotFoundException(
                    "Transfer not found."
            );
        }

        boolean hasSource = transactions.stream()
                .anyMatch(transaction ->
                        TransactionType.EXPENSE.equals(transaction.getType())
                );

        boolean hasDestination = transactions.stream()
                .anyMatch(transaction ->
                        TransactionType.INCOME.equals(transaction.getType())
                );

        if (!hasSource || !hasDestination) {
            throw new ResourceNotFoundException(
                    "Transfer not found."
            );
        }
    }

    private Transaction findByType(
            List<Transaction> transactions,
            TransactionType type
    ) {
        return transactions.stream()
                .filter(transaction -> type.equals(transaction.getType()))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Transfer transaction not found."
                        )
                );
    }

    private void updateTransaction(
            Transaction transaction,
            TransferRequest request,
            FinancialAccount financialAccount
    ) {
        transaction.setDescription(request.description());
        transaction.setAmount(request.amount());
        transaction.setTransactionDate(request.transactionDate());
        transaction.setFinancialAccount(financialAccount);
    }

    private TransferResponse toResponse(
            UUID transferId,
            TransferRequest request,
            FinancialAccount sourceAccount,
            FinancialAccount destinationAccount
    ) {
        return new TransferResponse(
                transferId,
                request.description(),
                request.amount(),
                request.transactionDate(),
                sourceAccount.getId(),
                sourceAccount.getName(),
                destinationAccount.getId(),
                destinationAccount.getName()
        );
    }

    private TransferResponse toResponse(
            UUID transferId,
            Transaction sourceTransaction,
            Transaction destinationTransaction
    ) {
        return new TransferResponse(
                transferId,
                sourceTransaction.getDescription(),
                sourceTransaction.getAmount(),
                sourceTransaction.getTransactionDate(),
                sourceTransaction.getFinancialAccount().getId(),
                sourceTransaction.getFinancialAccount().getName(),
                destinationTransaction.getFinancialAccount().getId(),
                destinationTransaction.getFinancialAccount().getName()
        );
    }
}