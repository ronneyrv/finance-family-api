package com.ronney.finance.service.impl;

import com.ronney.finance.domain.entity.Category;
import com.ronney.finance.domain.entity.CreditCard;
import com.ronney.finance.domain.entity.CreditCardInstallment;
import com.ronney.finance.domain.entity.FinancialAccount;
import com.ronney.finance.domain.entity.Purchase;
import com.ronney.finance.domain.entity.SubCategory;
import com.ronney.finance.domain.entity.Transaction;
import com.ronney.finance.domain.entity.User;
import com.ronney.finance.domain.enums.TransactionKind;
import com.ronney.finance.domain.enums.TransactionType;
import com.ronney.finance.dto.request.InvoicePaymentRequest;
import com.ronney.finance.dto.request.PurchaseCategoryRequest;
import com.ronney.finance.dto.request.PurchaseRequest;
import com.ronney.finance.dto.response.InstallmentResponse;
import com.ronney.finance.dto.response.InvoiceInstallmentResponse;
import com.ronney.finance.dto.response.InvoiceResponse;
import com.ronney.finance.dto.response.PendingPurchaseResponse;
import com.ronney.finance.exception.ResourceNotFoundException;
import com.ronney.finance.repository.CategoryRepository;
import com.ronney.finance.repository.CreditCardInstallmentRepository;
import com.ronney.finance.repository.CreditCardRepository;
import com.ronney.finance.repository.FinancialAccountRepository;
import com.ronney.finance.repository.PurchaseRepository;
import com.ronney.finance.repository.SubCategoryRepository;
import com.ronney.finance.repository.TransactionRepository;
import com.ronney.finance.service.CurrentUserService;
import com.ronney.finance.service.PurchaseService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final CreditCardRepository creditCardRepository;
    private final CreditCardInstallmentRepository installmentRepository;
    private final PurchaseRepository purchaseRepository;
    private final CurrentUserService currentUserService;
    private final FinancialAccountRepository financialAccountRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    @Override
    @Transactional
    public List<InstallmentResponse> createPurchase(
            UUID creditCardId,
            PurchaseRequest request
    ) {

        User user = currentUserService.getAuthenticatedUser();

        CreditCard card = creditCardRepository
                .findByIdAndUserId(
                        creditCardId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Credit card not found."
                        )
                );

        if (request.subCategoryId() != null && request.categoryId() == null) {
            throw new IllegalArgumentException(
                    "Category is required when SubCategory is provided."
            );
        }

        Category category = null;

        if (request.categoryId() != null) {
            category = categoryRepository
                    .findById(request.categoryId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Category not found."
                            )
                    );

            if (!TransactionType.EXPENSE.equals(category.getType())) {
                throw new IllegalArgumentException(
                        "Category does not match purchase type."
                );
            }
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

        Purchase purchase = Purchase.builder()
                .id(UUID.randomUUID())
                .description(request.description())
                .totalAmount(request.totalAmount())
                .purchaseDate(request.purchaseDate())
                .installmentCount(request.installments())
                .creditCard(card)
                .category(category)
                .subCategory(subCategory)
                .build();

        BigDecimal installmentAmount = request.totalAmount()
                .divide(
                        BigDecimal.valueOf(request.installments()),
                        2,
                        RoundingMode.HALF_UP
                );

        LocalDate invoiceDate = request.purchaseDate();

        if (request.purchaseDate().getDayOfMonth() > card.getClosingDay()) {
            invoiceDate = invoiceDate.plusMonths(1);
        }

        List<CreditCardInstallment> installments = new ArrayList<>();

        for (int i = 1; i <= request.installments(); i++) {

            LocalDate currentInvoice = invoiceDate.plusMonths(i - 1);

            BigDecimal currentInstallmentAmount = installmentAmount;

            if (i == request.installments()) {

                BigDecimal previousInstallmentsTotal =
                        installmentAmount.multiply(
                                BigDecimal.valueOf(request.installments() - 1L)
                        );

                currentInstallmentAmount = request.totalAmount().subtract(previousInstallmentsTotal);
            }

            CreditCardInstallment installment =
                    CreditCardInstallment.builder()
                            .id(UUID.randomUUID())
                            .amount(currentInstallmentAmount)
                            .installmentNumber(i)
                            .invoiceMonth(currentInvoice.getMonthValue())
                            .invoiceYear(currentInvoice.getYear())
                            .paid(false)
                            .purchase(purchase)
                            .build();

            installments.add(installment);
        }

        purchase.setInstallments(installments);

        Purchase savedPurchase = purchaseRepository.save(purchase);

        return savedPurchase
                .getInstallments()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(
            UUID creditCardId,
            Integer month,
            Integer year
    ) {

        User user = currentUserService.getAuthenticatedUser();

        CreditCard card = creditCardRepository
                .findByIdAndUserId(
                        creditCardId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Credit card not found."
                        )
                );

        List<CreditCardInstallment> installments = installmentRepository
                .findByPurchaseCreditCardIdAndInvoiceMonthAndInvoiceYear(
                        creditCardId,
                        month,
                        year
                );

        if (installments.isEmpty()) {

            return new InvoiceResponse(
                    card.getName(),
                    card.getClosingDay(),
                    card.getDueDay(),
                    month,
                    year,
                    calculateInvoiceDueDate(
                            month,
                            year,
                            card.getClosingDay(),
                            card.getDueDay()
                    ),
                    BigDecimal.ZERO,
                    calculateAvailableLimit(
                            creditCardId,
                            card
                    ),
                    List.of()
            );
        }

        BigDecimal total = installments.stream()
                .map(CreditCardInstallment::getAmount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        List<InvoiceInstallmentResponse> items = installments
                .stream()
                .map(i ->
                        new InvoiceInstallmentResponse(
                                i.getPurchase().getDescription(),
                                i.getInstallmentNumber() + "/" + i.getPurchase().getInstallmentCount(),
                                i.getAmount(),
                                i.getPaid(),
                                i.getPaidAt()
                        )
                )
                .toList();

        BigDecimal availableLimit =
                calculateAvailableLimit(
                        creditCardId,
                        card
                );

        return new InvoiceResponse(
                card.getName(),
                card.getClosingDay(),
                card.getDueDay(),
                month,
                year,
                calculateInvoiceDueDate(
                        month,
                        year,
                        card.getClosingDay(),
                        card.getDueDay()
                ),
                total,
                availableLimit,
                items
        );
    }

    @Override
    @Transactional
    public void payInvoice(
            UUID creditCardId,
            Integer month,
            Integer year,
            InvoicePaymentRequest request
    ) {
        User user = currentUserService.getAuthenticatedUser();

        FinancialAccount financialAccount = financialAccountRepository
                .findByIdAndUserId(
                        request.accountId(),
                        user.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Financial account not found."
                        )
                );

        CreditCard card = creditCardRepository
                .findByIdAndUserId(
                        creditCardId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Credit card not found."
                        )
                );

        List<CreditCardInstallment> installments = installmentRepository
                .findByPurchaseCreditCardIdAndInvoiceMonthAndInvoiceYear(
                                creditCardId,
                                month,
                                year
                        );

        if (installments.isEmpty()) {
            throw new ResourceNotFoundException("Invoice not found.");
        }

        if (installments.stream().allMatch(CreditCardInstallment::getPaid)) {
            throw new IllegalStateException("Invoice already paid.");
        }

        BigDecimal invoiceTotal = installments.stream()
                .map(CreditCardInstallment::getAmount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        LocalDate paymentDate = LocalDate.now();

        installments.forEach(installment -> {
            installment.setPaid(true);
            installment.setPaidAt(paymentDate);
        });

        installmentRepository.saveAll(installments);

        Transaction paymentTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .description(
                        buildInvoicePaymentDescription(
                                card,
                                month,
                                year
                        )
                )
                .amount(invoiceTotal)
                .transactionDate(paymentDate)
                .type(TransactionType.EXPENSE)
                .transactionKind(TransactionKind.CREDIT_CARD_PAYMENT)
                .paymentMethod(request.paymentMethod())
                .user(user)
                .category(null)
                .subCategory(null)
                .financialAccount(financialAccount)
                .build();

        transactionRepository.save(paymentTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PendingPurchaseResponse> findPendingPurchases() {
        User user = currentUserService.getAuthenticatedUser();

        return purchaseRepository
                .findDistinctByCreditCardUserIdAndInstallmentsPaidFalseOrderByPurchaseDateDesc(
                        user.getId()
                )
                .stream()
                .map(this::toPendingPurchaseResponse)
                .toList();
    }

    @Override
    @Transactional
    public void updateCategory(
            UUID purchaseId,
            PurchaseCategoryRequest request
    ) {
        User user = currentUserService.getAuthenticatedUser();

        Purchase purchase = purchaseRepository
                .findByIdAndCreditCardUserId(
                        purchaseId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Purchase not found."
                        )
                );

        if (request.subCategoryId() != null
                && request.categoryId() == null) {
            throw new IllegalArgumentException(
                    "Category is required when SubCategory is provided."
            );
        }

        Category category = null;

        if (request.categoryId() != null) {
            category = categoryRepository
                    .findById(request.categoryId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Category not found."
                            )
                    );

            if (!TransactionType.EXPENSE.equals(category.getType())) {
                throw new IllegalArgumentException(
                        "Category does not match purchase type."
                );
            }
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

        purchase.setCategory(category);
        purchase.setSubCategory(subCategory);

        purchaseRepository.save(purchase);
    }

    @Override
    @Transactional
    public void deletePurchase(UUID id) {
        User user = currentUserService.getAuthenticatedUser();

        Purchase purchase = purchaseRepository
                .findByIdAndCreditCardUserId(
                        id,
                        user.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Purchase not found."
                        )
                );

        purchaseRepository.delete(purchase);
    }

    private BigDecimal calculateAvailableLimit(
            UUID creditCardId,
            CreditCard card
    ) {

        List<CreditCardInstallment> openInstallments =
                installmentRepository.findByPurchaseCreditCardIdAndPaidFalse(
                        creditCardId
                );

        BigDecimal usedLimit = openInstallments.stream()
                .map(CreditCardInstallment::getAmount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        return card.getCreditLimit().subtract(usedLimit);
    }

    private String buildInvoicePaymentDescription(
            CreditCard card,
            int month,
            int year
    ) {
        return String.format(
                "Pagamento da fatura - %s (%02d/%d)",
                card.getName(),
                month,
                year
        );
    }

    private InstallmentResponse toResponse(
            CreditCardInstallment installment
    ) {

        return new InstallmentResponse(
                installment.getId(),
                installment.getPurchase().getDescription(),
                installment.getInstallmentNumber(),
                installment.getPurchase().getInstallmentCount(),
                installment.getAmount(),
                installment.getInvoiceMonth(),
                installment.getInvoiceYear(),
                installment.getPaid()
        );
    }

    private PendingPurchaseResponse toPendingPurchaseResponse(
            Purchase purchase
    ) {
        CreditCard card = purchase.getCreditCard();

        return new PendingPurchaseResponse(
                purchase.getId(),
                purchase.getDescription(),
                purchase.getPurchaseDate(),
                purchase.getTotalAmount(),
                purchase.getInstallmentCount(),
                card.getId(),
                card.getName(),
                purchase.getCategory() != null
                        ? purchase.getCategory().getId()
                        : null,
                purchase.getCategory() != null
                        ? purchase.getCategory().getName()
                        : null,
                purchase.getSubCategory() != null
                        ? purchase.getSubCategory().getId()
                        : null,
                purchase.getSubCategory() != null
                        ? purchase.getSubCategory().getName()
                        : null
        );
    }

    private LocalDate calculateInvoiceDueDate(
            Integer invoiceMonth,
            Integer invoiceYear,
            Integer closingDay,
            Integer dueDay
    ) {
        LocalDate invoiceDate = LocalDate.of(
                invoiceYear,
                invoiceMonth,
                1
        );

        if (dueDay < closingDay) {
            invoiceDate = invoiceDate.plusMonths(1);
        }

        int lastDayOfMonth = invoiceDate.lengthOfMonth();
        int effectiveDueDay = Math.min(dueDay, lastDayOfMonth);

        return invoiceDate.withDayOfMonth(effectiveDueDay);
    }
}