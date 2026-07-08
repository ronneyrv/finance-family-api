package com.ronney.finance.service.impl;

import com.ronney.finance.domain.entity.Category;
import com.ronney.finance.domain.entity.RecurringTransaction;
import com.ronney.finance.domain.entity.SubCategory;
import com.ronney.finance.domain.entity.User;
import com.ronney.finance.domain.enums.PaymentMethod;
import com.ronney.finance.domain.enums.TransactionType;
import com.ronney.finance.dto.request.RecurringTransactionRequest;
import com.ronney.finance.dto.response.RecurringTransactionResponse;
import com.ronney.finance.exception.ResourceNotFoundException;
import com.ronney.finance.repository.CategoryRepository;
import com.ronney.finance.repository.RecurringTransactionRepository;
import com.ronney.finance.repository.SubCategoryRepository;
import com.ronney.finance.service.CurrentUserService;
import com.ronney.finance.service.RecurringTransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecurringTransactionServiceImpl
        implements RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public RecurringTransactionResponse create(
            RecurringTransactionRequest request
    ) {
        User user = currentUserService.getAuthenticatedUser();

        validateRequest(request);

        Category category = findCategory(request.categoryId());

        validateCategoryType(category, request.type());

        SubCategory subCategory = findSubCategory(
                request.subCategoryId()
        );

        validateSubCategory(category, subCategory);

        RecurringTransaction recurringTransaction =
                RecurringTransaction.builder()
                        .id(UUID.randomUUID())
                        .description(request.description())
                        .amount(request.amount())
                        .type(request.type())
                        .paymentMethod(request.paymentMethod())
                        .dayOfMonth(request.dayOfMonth())
                        .startDate(request.startDate())
                        .endDate(request.endDate())
                        .active(true)
                        .user(user)
                        .category(category)
                        .subCategory(subCategory)
                        .build();

        recurringTransaction =
                recurringTransactionRepository.save(
                        recurringTransaction
                );

        return toResponse(recurringTransaction);
    }

    @Override
    @Transactional
    public List<RecurringTransactionResponse> findAll() {
        User user = currentUserService.getAuthenticatedUser();

        return recurringTransactionRepository
                .findByUserIdOrderByDayOfMonthAsc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RecurringTransactionResponse findById(
            UUID id
    ) {
        User user = currentUserService.getAuthenticatedUser();

        RecurringTransaction recurringTransaction =
                recurringTransactionRepository
                        .findByIdAndUserId(id, user.getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Recurring transaction not found."
                                )
                        );

        return toResponse(recurringTransaction);
    }

    @Override
    @Transactional
    public RecurringTransactionResponse update(
            UUID id,
            RecurringTransactionRequest request
    ) {
        User user = currentUserService.getAuthenticatedUser();

        RecurringTransaction recurringTransaction =
                recurringTransactionRepository
                        .findByIdAndUserId(id, user.getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Recurring transaction not found."
                                )
                        );

        validateRequest(request);

        Category category = findCategory(request.categoryId());

        validateCategoryType(category, request.type());

        SubCategory subCategory = findSubCategory(
                request.subCategoryId()
        );

        validateSubCategory(category, subCategory);

        recurringTransaction.setDescription(request.description());
        recurringTransaction.setAmount(request.amount());
        recurringTransaction.setType(request.type());
        recurringTransaction.setPaymentMethod(request.paymentMethod());
        recurringTransaction.setDayOfMonth(request.dayOfMonth());
        recurringTransaction.setStartDate(request.startDate());
        recurringTransaction.setEndDate(request.endDate());
        recurringTransaction.setCategory(category);
        recurringTransaction.setSubCategory(subCategory);

        recurringTransaction =
                recurringTransactionRepository.save(
                        recurringTransaction
                );

        return toResponse(recurringTransaction);
    }

    @Override
    @Transactional
    public RecurringTransactionResponse updateStatus(
            UUID id,
            boolean active
    ) {
        User user = currentUserService.getAuthenticatedUser();

        RecurringTransaction recurringTransaction =
                recurringTransactionRepository
                        .findByIdAndUserId(id, user.getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Recurring transaction not found."
                                )
                        );

        recurringTransaction.setActive(active);

        recurringTransaction =
                recurringTransactionRepository.save(
                        recurringTransaction
                );

        return toResponse(recurringTransaction);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = currentUserService.getAuthenticatedUser();

        RecurringTransaction recurringTransaction =
                recurringTransactionRepository
                        .findByIdAndUserId(id, user.getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Recurring transaction not found."
                                )
                        );

        recurringTransactionRepository.delete(recurringTransaction);
    }

    private Category findCategory(UUID categoryId) {
        return categoryRepository
                .findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found."
                        )
                );
    }

    private SubCategory findSubCategory(
            UUID subCategoryId
    ) {
        if (subCategoryId == null) {
            return null;
        }

        return subCategoryRepository
                .findById(subCategoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "SubCategory not found."
                        )
                );
    }

    private void validateRequest(
            RecurringTransactionRequest request
    ) {
        if (request.endDate() != null
                && request.endDate().isBefore(request.startDate())) {

            throw new IllegalArgumentException(
                    "End date must not be before start date."
            );
        }

        if (request.type() == TransactionType.INCOME
                && request.paymentMethod() == PaymentMethod.DEBIT_CARD) {

            throw new IllegalArgumentException(
                    "Debit card is not allowed for income transactions."
            );
        }
    }

    private void validateCategoryType(
            Category category,
            TransactionType type
    ) {
        if (!category.getType().equals(type)) {
            throw new IllegalArgumentException(
                    "Category does not match transaction type."
            );
        }
    }

    private void validateSubCategory(
            Category category,
            SubCategory subCategory
    ) {
        if (subCategory != null
                && !subCategory.getCategory()
                .getId()
                .equals(category.getId())) {

            throw new IllegalArgumentException(
                    "SubCategory does not belong to Category."
            );
        }
    }

    private RecurringTransactionResponse toResponse(
            RecurringTransaction recurringTransaction
    ) {
        return new RecurringTransactionResponse(
                recurringTransaction.getId(),
                recurringTransaction.getDescription(),
                recurringTransaction.getAmount(),
                recurringTransaction.getType(),
                recurringTransaction.getPaymentMethod(),
                recurringTransaction.getDayOfMonth(),
                recurringTransaction.getStartDate(),
                recurringTransaction.getEndDate(),
                recurringTransaction.getActive(),
                recurringTransaction.getCategory().getId(),
                recurringTransaction.getCategory().getName(),
                recurringTransaction.getSubCategory() != null
                        ? recurringTransaction.getSubCategory().getId()
                        : null,
                recurringTransaction.getSubCategory() != null
                        ? recurringTransaction.getSubCategory().getName()
                        : null
        );
    }
}