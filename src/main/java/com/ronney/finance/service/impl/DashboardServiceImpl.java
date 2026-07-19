package com.ronney.finance.service.impl;

import com.ronney.finance.domain.entity.CreditCard;
import com.ronney.finance.domain.entity.CreditCardInstallment;
import com.ronney.finance.domain.entity.RecurringTransaction;
import com.ronney.finance.domain.entity.User;
import com.ronney.finance.domain.enums.PaymentMethod;
import com.ronney.finance.domain.enums.TransactionKind;
import com.ronney.finance.domain.enums.TransactionType;
import com.ronney.finance.dto.response.*;
import com.ronney.finance.repository.CreditCardInstallmentRepository;
import com.ronney.finance.repository.CreditCardRepository;
import com.ronney.finance.repository.RecurringTransactionRepository;
import com.ronney.finance.repository.TransactionRepository;
import com.ronney.finance.service.CurrentUserService;
import com.ronney.finance.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final CreditCardInstallmentRepository installmentRepository;
    private final CreditCardRepository creditCardRepository;

    private boolean occursInMonth(
            RecurringTransaction recurringTransaction,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        int effectiveDay = Math.min(
                recurringTransaction.getDayOfMonth(),
                periodEnd.lengthOfMonth()
        );

        LocalDate occurrenceDate =
                periodStart.withDayOfMonth(effectiveDay);

        if (occurrenceDate.isBefore(
                recurringTransaction.getStartDate()
        )) {
            return false;
        }

        return recurringTransaction.getEndDate() == null
                || !occurrenceDate.isAfter(
                recurringTransaction.getEndDate()
        );
    }

    @Override
    public DashboardSummaryResponse getSummary() {
        User user = currentUserService.getAuthenticatedUser();

        BigDecimal totalIncome =
                transactionRepository.sumByUserIdAndType(
                        user.getId(),
                        TransactionType.INCOME,
                        TransactionKind.REGULAR
                );

        BigDecimal totalExpense =
                transactionRepository.sumByUserIdAndType(
                        user.getId(),
                        TransactionType.EXPENSE,
                        TransactionKind.REGULAR
                );

        BigDecimal balance =
                totalIncome.subtract(totalExpense);

        BigDecimal cashIncome =
                transactionRepository
                        .sumByUserIdAndTypeAndPaymentMethods(
                                user.getId(),
                                TransactionType.INCOME,
                                List.of(PaymentMethod.CASH)
                        );

        BigDecimal cashExpense =
                transactionRepository
                        .sumByUserIdAndTypeAndPaymentMethods(
                                user.getId(),
                                TransactionType.EXPENSE,
                                List.of(PaymentMethod.CASH)
                        );

        BigDecimal cashBalance =
                cashIncome.subtract(cashExpense);

        BigDecimal bankIncome =
                transactionRepository
                        .sumByUserIdAndTypeAndPaymentMethods(
                                user.getId(),
                                TransactionType.INCOME,
                                List.of(
                                        PaymentMethod.PIX,
                                        PaymentMethod.BANK_TRANSFER
                                )
                        );

        BigDecimal bankExpense =
                transactionRepository
                        .sumByUserIdAndTypeAndPaymentMethods(
                                user.getId(),
                                TransactionType.EXPENSE,
                                List.of(
                                        PaymentMethod.PIX,
                                        PaymentMethod.BANK_TRANSFER,
                                        PaymentMethod.DEBIT_CARD
                                )
                        );

        BigDecimal bankBalance =
                bankIncome.subtract(bankExpense);

        return new DashboardSummaryResponse(
                totalIncome,
                totalExpense,
                balance,
                cashBalance,
                bankBalance
        );
    }

    @Override
    public List<CategoryExpenseResponse> getExpensesByCategory() {
        User user = currentUserService.getAuthenticatedUser();

        return transactionRepository.findExpensesByCategory(
                user.getId()
        )
                .stream()
                .map(
                        item -> new CategoryExpenseResponse(
                                item.getCategory(),
                                item.getAmount()
                        )
                )
                .toList();
    }

    @Override
    public List<MonthlySummaryResponse> getMonthlySummary(Integer year) {
        User user = currentUserService.getAuthenticatedUser();

        return transactionRepository.findMonthlySummary(
                user.getId(),
                year
        )
                .stream()
                .map(item -> {BigDecimal balance = item.getIncome()
                        .subtract(item.getExpense()
                        );
                return new MonthlySummaryResponse(
                        Month.of(item.getMonth()),
                        item.getIncome(),
                        item.getExpense(),
                        balance
                     );
                })
                .toList();
    }

    @Override
    public List<MonthlyProjectionResponse> getProjection(
            Integer year
    ) {
        User user = currentUserService.getAuthenticatedUser();

        List<MonthlyProjectionResponse> projections =
                new ArrayList<>();

        for (int month = 1; month <= 12; month++) {

            LocalDate periodStart =
                    LocalDate.of(year, month, 1);

            LocalDate periodEnd =
                    periodStart.withDayOfMonth(
                            periodStart.lengthOfMonth()
                    );

            List<RecurringTransaction> recurringTransactions =
                    recurringTransactionRepository.findActiveForPeriod(
                            user.getId(),
                            periodStart,
                            periodEnd
                    );

            BigDecimal projectedIncome =
                    recurringTransactions.stream()
                            .filter(recurringTransaction ->
                                    recurringTransaction.getType()
                                            == TransactionType.INCOME
                            )
                            .filter(recurringTransaction ->
                                    occursInMonth(
                                            recurringTransaction,
                                            periodStart,
                                            periodEnd
                                    )
                            )
                            .map(RecurringTransaction::getAmount)
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            BigDecimal projectedRecurringExpense =
                    recurringTransactions.stream()
                            .filter(recurringTransaction ->
                                    recurringTransaction.getType()
                                            == TransactionType.EXPENSE
                            )
                            .filter(recurringTransaction ->
                                    occursInMonth(
                                            recurringTransaction,
                                            periodStart,
                                            periodEnd
                                    )
                            )
                            .map(RecurringTransaction::getAmount)
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            List<CreditCardInstallment> installments =
                    installmentRepository
                            .findByPurchaseCreditCardUserIdAndInvoiceMonthAndInvoiceYear(
                                    user.getId(),
                                    month,
                                    year
                            );

            BigDecimal projectedCreditCardExpense =
                    installments.stream()
                            .filter(installment ->
                                    !installment.getPaid()
                            )
                            .map(CreditCardInstallment::getAmount)
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            BigDecimal projectedTotalExpense =
                    projectedRecurringExpense.add(
                            projectedCreditCardExpense
                    );

            BigDecimal projectedBalance =
                    projectedIncome.subtract(
                            projectedTotalExpense
                    );

            projections.add(
                    new MonthlyProjectionResponse(
                            Month.of(month),
                            projectedIncome,
                            projectedRecurringExpense,
                            projectedCreditCardExpense,
                            projectedTotalExpense,
                            projectedBalance
                    )
            );
        }

        return projections;
    }

    @Override
    public DashboardFiltersResponse getFilters() {
        User user = currentUserService.getAuthenticatedUser();

        YearMonth now = YearMonth.now();
        int currentYear = now.getYear();

        int firstYear = transactionRepository
                .findFirstTransactionDate(user.getId())
                .map(LocalDate::getYear)
                .orElse(currentYear);

        List<Integer> years = IntStream
                .rangeClosed(firstYear, currentYear + 1)
                .boxed()
                .toList();

        List<Integer> months = IntStream
                .rangeClosed(1, 12)
                .boxed()
                .toList();

        return new DashboardFiltersResponse(
                years,
                months,
                now.getYear(),
                now.getMonthValue()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditCardInvoiceSummaryResponse> getCreditCardSummaries() {

        User user = currentUserService.getAuthenticatedUser();

        YearMonth now = YearMonth.now();

        List<CreditCard> creditCards =
                creditCardRepository.findByUserId(
                        user.getId()
                );

        return creditCards.stream()
                .map(card -> {

                    List<CreditCardInstallment> installments =
                            installmentRepository
                                    .findByPurchaseCreditCardIdAndInvoiceMonthAndInvoiceYear(
                                            card.getId(),
                                            now.getMonthValue(),
                                            now.getYear()
                                    );

                    BigDecimal invoiceAmount =
                            installments.stream()
                                    .filter(installment ->
                                            !installment.getPaid()
                                    )
                                    .map(CreditCardInstallment::getAmount)
                                    .reduce(
                                            BigDecimal.ZERO,
                                            BigDecimal::add
                                    );

                    long installmentCount =
                            installments.stream()
                                    .filter(installment ->
                                            !installment.getPaid()
                                    )
                                    .count();

                    return new CreditCardInvoiceSummaryResponse(
                            card.getId(),
                            card.getName(),
                            invoiceAmount,
                            (int) installmentCount,
                            card.getDueDay(),
                            installmentCount > 0
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyCreditCardTrendResponse> getCreditCardTrend(
            Integer year
    ) {

        User user = currentUserService.getAuthenticatedUser();

        List<CreditCard> creditCards =
                creditCardRepository.findByUserId(user.getId());

        List<CreditCardInstallment> installments =
                installmentRepository
                        .findByPurchaseCreditCardUserIdAndInvoiceYear(
                                user.getId(),
                                year
                        );

        Map<UUID, CreditCard> cardsById =
                creditCards.stream()
                        .collect(Collectors.toMap(
                                CreditCard::getId,
                                Function.identity()
                        ));

        Map<Month, Map<UUID, BigDecimal>> grouped =
                new EnumMap<>(Month.class);

        for (CreditCardInstallment installment : installments) {

            Month month = Month.of(
                    installment.getInvoiceMonth()
            );

            UUID cardId = installment
                    .getPurchase()
                    .getCreditCard()
                    .getId();

            grouped
                    .computeIfAbsent(
                            month,
                            m -> new HashMap<>()
                    )
                    .merge(
                            cardId,
                            installment.getAmount(),
                            BigDecimal::add
                    );
        }

        List<MonthlyCreditCardTrendResponse> response = new ArrayList<>();

        for (Month month : Month.values()) {

            Map<UUID, BigDecimal> monthData =
                    grouped.getOrDefault(
                            month,
                            Collections.emptyMap()
                    );

            List<CreditCardMonthlyExpenseResponse> cards =
                    monthData.entrySet()
                            .stream()
                            .map(entry -> {

                                CreditCard card = cardsById.get(entry.getKey());

                                return new CreditCardMonthlyExpenseResponse(
                                        card.getName(),
                                        entry.getValue()
                                );
                            })
                            .sorted(Comparator.comparing(
                                    CreditCardMonthlyExpenseResponse::cardName
                            ))
                            .toList();

            BigDecimal total =
                    monthData.values()
                            .stream()
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            response.add(
                    new MonthlyCreditCardTrendResponse(
                            month,
                            total,
                            cards
                    )
            );
        }

        return response;
    }
}
