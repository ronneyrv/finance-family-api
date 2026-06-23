package com.ronney.finance.service.impl;

import com.ronney.finance.domain.entity.Category;
import com.ronney.finance.domain.entity.SubCategory;
import com.ronney.finance.domain.entity.Transaction;
import com.ronney.finance.domain.entity.User;
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
                .user(user)
                .category(category)
                .subCategory(subCategory)
                .build();

        transaction = transactionRepository.save(transaction);

        return toResponse(transaction);

    }

    @Override
    public Page<TransactionResponse> findAll(
            Pageable pageable,
            LocalDate startDate,
            LocalDate endDate
    ) {
        throw new UnsupportedOperationException(
                "Not implemented yet"
        );
    }

    @Override
    public TransactionResponse findById(UUID id) {
        throw new UnsupportedOperationException(
                "Not implemented yet"
        );
    }

    @Override
    public TransactionResponse update(
            UUID id,
            TransactionRequest request
    ) {
        throw new UnsupportedOperationException(
                "Not implemented yet"
        );
    }

    @Override
    public void delete(UUID id) {
        throw new UnsupportedOperationException(
                "Not implemented yet"
        );
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
                transaction.getCategory().getId(),
                transaction.getCategory().getName(),
                transaction.getSubCategory() != null ? transaction.getSubCategory().getId() : null,
                transaction.getSubCategory() != null ? transaction.getSubCategory().getName() : null
        );
    }
}
