package com.ronney.finance.service.impl;

import com.ronney.finance.domain.entity.Category;
import com.ronney.finance.domain.entity.SubCategory;
import com.ronney.finance.domain.entity.Transaction;
import com.ronney.finance.domain.entity.User;
import com.ronney.finance.domain.enums.PaymentMethod;
import com.ronney.finance.domain.enums.TransactionType;
import com.ronney.finance.dto.request.TransactionRequest;
import com.ronney.finance.dto.response.TransactionResponse;
import com.ronney.finance.exception.ResourceNotFoundException;
import com.ronney.finance.repository.CategoryRepository;
import com.ronney.finance.repository.SubCategoryRepository;
import com.ronney.finance.repository.TransactionRepository;
import com.ronney.finance.service.CurrentUserService;
import com.ronney.finance.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public TransactionResponse create(TransactionRequest request) {

        User user = currentUserService.getAuthenticatedUser();

        validatePaymentMethod(request);

        Category category = categoryRepository
                .findById(request.categoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found."
                        )
                );

        if (!category.getType().equals(request.type())) {
            throw new IllegalArgumentException(
                    "Category does not match transaction type."
            );
        }

        SubCategory subCategory = null;

        if (request.subCategoryId() != null) {
            subCategory = subCategoryRepository
                    .findById(request.subCategoryId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "SubCategory not found."
                            )
                    );
        }

        if (subCategory != null
                && !subCategory.getCategory()
                .getId()
                .equals(category.getId())) {

            throw new IllegalArgumentException(
                    "SubCategory does not belong to Category."
            );
        }

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .description(request.description())
                .amount(request.amount())
                .transactionDate(request.transactionDate())
                .type(request.type())
                .paymentMethod(request.paymentMethod())
                .user(user)
                .category(category)
                .subCategory(subCategory)
                .build();

        transaction = transactionRepository.save(transaction);

        return toResponse(transaction);

    }

    @Override
    @Transactional
    public Page<TransactionResponse> findAll(
            Pageable pageable,
            LocalDate startDate,
            LocalDate endDate
    ) {
        User user = currentUserService.getAuthenticatedUser();

        Page<Transaction> transactions;

        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByUserIdAndTransactionDateBetween(
                    user.getId(),
                    startDate,
                    endDate,
                    pageable
            );
        } else {
            transactions = transactionRepository.findByUserId(
                    user.getId(),
                    pageable
            );
        }
        return transactions.map(this::toResponse);
    }

    @Override
    @Transactional
    public TransactionResponse findById(UUID id) {
        User user = currentUserService.getAuthenticatedUser();

        Transaction transaction = transactionRepository.findByIdAndUserId(
                id,
                user.getId()
        )
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found.")
        );
        return toResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse update(
            UUID id,
            TransactionRequest request
    ) {
        User user = currentUserService.getAuthenticatedUser();

        validatePaymentMethod(request);

        Transaction transaction = transactionRepository.findByIdAndUserId(
                id,
                user.getId()
        )
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found.")
        );

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow (() -> new ResourceNotFoundException("Category not found.")
        );

        if (!category.getType().equals(request.type())) {
            throw new IllegalArgumentException("Category does not match transaction type.");
        }

        SubCategory subCategory = null;

        if (request.subCategoryId() != null) {
            subCategory = subCategoryRepository.findById(request.subCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found.")
            );
        }

        if (subCategory != null && !subCategory.getCategory().getId().equals(category.getId())) {
            throw new IllegalArgumentException("SubCategory does not belong to Category.");
        }

        transaction.setDescription(request.description());

        transaction.setAmount(request.amount());

        transaction.setTransactionDate(request.transactionDate());

        transaction.setType(request.type());

        transaction.setPaymentMethod(request.paymentMethod());

        transaction.setCategory(category);

        transaction.setSubCategory(subCategory);

        transaction = transactionRepository.save(transaction);

        return toResponse(transaction);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = currentUserService.getAuthenticatedUser();

        Transaction transaction = transactionRepository.findByIdAndUserId(
                id,
                user.getId()
        )
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found.")
        );

        transactionRepository.delete(transaction);
    }

    private void validatePaymentMethod(
            TransactionRequest request
    ) {
        if (request.paymentMethod() == null) {
            throw new IllegalArgumentException(
                    "Payment method is required for all transactions."
            );
        }

        if (request.type() == TransactionType.INCOME
                && request.paymentMethod() == PaymentMethod.DEBIT_CARD) {

            throw new IllegalArgumentException(
                    "Debit card is not allowed for income transactions."
            );
        }
    }

    private TransactionResponse toResponse(
            Transaction transaction
    ) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getType(),
                transaction.getPaymentMethod(),
                transaction.getCategory().getId(),
                transaction.getCategory().getName(),
                transaction.getSubCategory() != null ? transaction.getSubCategory().getId() : null,
                transaction.getSubCategory() != null ? transaction.getSubCategory().getName() : null
        );
    }
}
