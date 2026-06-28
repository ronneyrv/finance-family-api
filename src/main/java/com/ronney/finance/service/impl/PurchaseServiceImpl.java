package com.ronney.finance.service.impl;

import com.ronney.finance.domain.entity.CreditCard;
import com.ronney.finance.domain.entity.CreditCardInstallment;
import com.ronney.finance.domain.entity.User;
import com.ronney.finance.dto.request.PurchaseRequest;
import com.ronney.finance.dto.response.InstallmentResponse;
import com.ronney.finance.dto.response.InvoiceInstallmentResponse;
import com.ronney.finance.dto.response.InvoiceResponse;
import com.ronney.finance.exception.ResourceNotFoundException;
import com.ronney.finance.repository.CreditCardInstallmentRepository;
import com.ronney.finance.repository.CreditCardRepository;
import com.ronney.finance.service.CurrentUserService;
import com.ronney.finance.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    private final CurrentUserService currentUserService;

    @Override
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

            LocalDate currentInvoice =
                    invoiceDate.plusMonths(i - 1);

            CreditCardInstallment installment =
                    CreditCardInstallment.builder()
                            .id(UUID.randomUUID())
                            .description(request.description())
                            .amount(installmentAmount)
                            .installmentNumber(i)
                            .totalInstallments(request.installments())
                            .invoiceMonth(currentInvoice.getMonthValue())
                            .invoiceYear(currentInvoice.getYear())
                            .purchaseDate(request.purchaseDate())
                            .paid(false)
                            .creditCard(card)
                            .build();

            installments.add(installment);
        }

        installmentRepository.saveAll(installments);

        return installments
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
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
                        .findByCreditCardIdAndInvoiceMonthAndInvoiceYear(
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
                .findByCreditCardIdAndPaidFalse(
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
                                i.getDescription(),
                                i.getInstallmentNumber() + "/" + i.getTotalInstallments(),
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
    public void payInvoice(
            UUID creditCardId,
            Integer month,
            Integer year
    ) {
        User user = currentUserService.getAuthenticatedUser();

        creditCardRepository.findByIdAndUserId(
                creditCardId,
                user.getId()
        )
                .orElseThrow(() -> new ResourceNotFoundException("Credit card not found.")
        );

        List<CreditCardInstallment> installments = installmentRepository.findByCreditCardIdAndInvoiceMonthAndInvoiceYear(
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

        LocalDate paymentDate = LocalDate.now();

        installments.forEach(installment -> {
            installment.setPaid(true);
            installment.setPaidAt(paymentDate);
        });

        installmentRepository.saveAll(installments);
    }

    private InstallmentResponse toResponse(
            CreditCardInstallment installment
    ) {

        return new InstallmentResponse(
                installment.getId(),
                installment.getDescription(),
                installment.getInstallmentNumber(),
                installment.getTotalInstallments(),
                installment.getAmount(),
                installment.getInvoiceMonth(),
                installment.getInvoiceYear(),
                installment.getPaid()
        );
    }
}