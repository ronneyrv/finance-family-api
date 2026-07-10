package com.ronney.finance.service.impl;

import com.ronney.finance.domain.entity.CreditCard;
import com.ronney.finance.domain.entity.CreditCardInstallment;
import com.ronney.finance.domain.entity.FinancialAccount;
import com.ronney.finance.domain.entity.Purchase;
import com.ronney.finance.domain.entity.Transaction;
import com.ronney.finance.domain.entity.User;
import com.ronney.finance.domain.enums.TransactionKind;
import com.ronney.finance.domain.enums.TransactionType;
import com.ronney.finance.dto.request.InvoicePaymentRequest;
import com.ronney.finance.dto.request.PurchaseRequest;
import com.ronney.finance.dto.response.InstallmentResponse;
import com.ronney.finance.dto.response.InvoiceInstallmentResponse;
import com.ronney.finance.dto.response.InvoiceResponse;
import com.ronney.finance.exception.ResourceNotFoundException;
import com.ronney.finance.repository.CreditCardInstallmentRepository;
import com.ronney.finance.repository.CreditCardRepository;
import com.ronney.finance.repository.FinancialAccountRepository;
import com.ronney.finance.repository.PurchaseRepository;
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

        Purchase purchase = Purchase.builder()
                .id(UUID.randomUUID())
                .description(request.description())
                .totalAmount(request.totalAmount())
                .purchaseDate(request.purchaseDate())
                .installmentCount(request.installments())
                .creditCard(card)
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
            throw new ResourceNotFoundException("Invoice not found.");
        }

        BigDecimal total = installments.stream()
                .map(CreditCardInstallment::getAmount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        List<CreditCardInstallment> openInstallments = installmentRepository
                .findByPurchaseCreditCardIdAndPaidFalse(
                        creditCardId
                );

        BigDecimal usedLimit = openInstallments.stream()
                .map(CreditCardInstallment::getAmount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        BigDecimal availableLimit = card.getCreditLimit().subtract(usedLimit);

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

        return new InvoiceResponse(
                card.getName(),
                card.getClosingDay(),
                card.getDueDay(),
                month,
                year,
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
                        "Credit card invoice payment - "
                                + card.getName()
                                + " "
                                + month
                                + "/"
                                + year
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
}