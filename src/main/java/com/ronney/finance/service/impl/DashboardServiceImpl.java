package com.ronney.finance.service.impl;

import com.ronney.finance.domain.entity.CreditCard;
import com.ronney.finance.domain.entity.CreditCardInstallment;
import com.ronney.finance.domain.entity.RecurringTransaction;
import com.ronney.finance.domain.entity.User;
import com.ronney.finance.domain.enums.CommitmentLevel;
import com.ronney.finance.domain.enums.PaymentMethod;
import com.ronney.finance.domain.enums.TransactionKind;
import com.ronney.finance.domain.enums.TransactionType;
import com.ronney.finance.dto.response.*;
import com.ronney.finance.repository.CreditCardInstallmentRepository;
import com.ronney.finance.repository.CreditCardRepository;
import com.ronney.finance.repository.RecurringTransactionRepository;
import com.ronney.finance.repository.TransactionRepository;
import com.ronney.finance.repository.projection.MonthlySummaryProjection;
import com.ronney.finance.service.CurrentUserService;
import com.ronney.finance.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private static final BigDecimal LOW_COMMITMENT_LIMIT =
            BigDecimal.valueOf(30);

    private static final BigDecimal MEDIUM_COMMITMENT_LIMIT =
            BigDecimal.valueOf(60);

    private final TransactionRepository transactionRepository;
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final CreditCardRepository creditCardRepository;
    private final CreditCardInstallmentRepository installmentRepository;
    private final CurrentUserService currentUserService;

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

    private BigDecimal calculateRecurringAmount(
            List<RecurringTransaction> recurringTransactions,
            TransactionType type,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        return recurringTransactions.stream()
                .filter(recurringTransaction ->
                        recurringTransaction.getType() == type
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
    }

    private CashFlowResponse toCashFlowResponse(
            MonthlySummaryProjection projection
    ) {

        return new CashFlowResponse(
                Month.of(projection.getMonth()),
                projection.getIncome(),
                projection.getExpense()
        );
    }

    private List<CashFlowResponse> completeMonthlySeries(
            Map<Month, MonthlySummaryProjection> monthlySummary
    ) {

        return Stream.of(Month.values())
                .map(month -> {

                    MonthlySummaryProjection projection =
                            monthlySummary.get(month);

                    if (projection == null) {
                        return new CashFlowResponse(
                                month,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO
                        );
                    }

                    return toCashFlowResponse(projection);
                })
                .toList();
    }

    private Map<Month, CashFlowResponse> getMonthlyCashFlow(
            UUID householdId,
            Integer year
    ) {

        return transactionRepository
                .findHouseholdMonthlySummary(
                        householdId,
                        year
                )
                .stream()
                .map(this::toCashFlowResponse)
                .collect(
                        Collectors.toMap(
                                CashFlowResponse::month,
                                Function.identity()
                        )
                );
    }

    private Map<Month, MonthlySummaryProjection> getHouseholdMonthlySummary(
            UUID householdId,
            Integer year
    ) {

        return transactionRepository
                .findHouseholdMonthlySummary(
                        householdId,
                        year
                )
                .stream()
                .collect(
                        Collectors.toMap(
                                projection -> Month.of(projection.getMonth()),
                                Function.identity()
                        )
                );
    }

    private List<CumulativeResultResponse> buildCumulativeResult(
            Map<Month, MonthlySummaryProjection> monthlySummary
    ) {

        BigDecimal accumulated = BigDecimal.ZERO;

        List<CumulativeResultResponse> result = new ArrayList<>();

        for (Month month : Month.values()) {

            MonthlySummaryProjection projection =
                    monthlySummary.get(month);

            BigDecimal income =
                    projection != null
                            ? projection.getIncome()
                            : BigDecimal.ZERO;

            BigDecimal expense =
                    projection != null
                            ? projection.getExpense()
                            : BigDecimal.ZERO;

            BigDecimal monthlyResult =
                    income.subtract(expense);

            accumulated =
                    accumulated.add(monthlyResult);

            result.add(
                    new CumulativeResultResponse(
                            month,
                            income,
                            expense,
                            monthlyResult,
                            accumulated
                    )
            );
        }

        return result;
    }

    private IncomeCommitmentResponse toIncomeCommitmentResponse(
            BigDecimal monthlyIncome,
            BigDecimal recurringExpenses,
            BigDecimal unpaidCreditCardInstallments
    ) {

        BigDecimal monthlyCommitments =
                recurringExpenses.add(
                        unpaidCreditCardInstallments
                );

        BigDecimal availableIncome =
                monthlyIncome.subtract(
                        monthlyCommitments
                );

        BigDecimal commitmentPercentage =
                calculateCommitmentPercentage(
                        monthlyIncome,
                        monthlyCommitments
                );

        CommitmentLevel commitmentLevel =
                calculateCommitmentLevel(
                        commitmentPercentage
                );

        return new IncomeCommitmentResponse(
                monthlyIncome,
                recurringExpenses,
                unpaidCreditCardInstallments,
                monthlyCommitments,
                availableIncome,
                commitmentPercentage,
                commitmentLevel
        );
    }

    private BigDecimal calculateCommitmentPercentage(
            BigDecimal monthlyIncome,
            BigDecimal monthlyCommitments
    ) {

        if (monthlyIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return monthlyCommitments
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        monthlyIncome,
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private CommitmentLevel calculateCommitmentLevel(
            BigDecimal commitmentPercentage
    ) {

        if (commitmentPercentage.compareTo(
                LOW_COMMITMENT_LIMIT
        ) <= 0) {
            return CommitmentLevel.LOW;
        }

        if (commitmentPercentage.compareTo(
                MEDIUM_COMMITMENT_LIMIT
        ) <= 0) {
            return CommitmentLevel.MEDIUM;
        }

        return CommitmentLevel.HIGH;
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
    public List<MonthlySummaryResponse> getMonthlySummary(
            Integer year
    ) {
        User user = currentUserService.getAuthenticatedUser();

        Map<Month, MonthlySummaryProjection> summaryByMonth =
                transactionRepository.findMonthlySummary(
                                user.getId(),
                                year
                        )
                        .stream()
                        .collect(Collectors.toMap(
                                item -> Month.of(item.getMonth()),
                                Function.identity()
                        ));

        List<MonthlySummaryResponse> response =
                new ArrayList<>();

        for (Month month : Month.values()) {

            MonthlySummaryProjection summary =
                    summaryByMonth.get(month);

            if (summary == null) {
                response.add(
                        new MonthlySummaryResponse(
                                month,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO
                        )
                );

                continue;
            }

            response.add(
                    new MonthlySummaryResponse(
                            month,
                            summary.getIncome(),
                            summary.getExpense(),
                            summary.getIncome().subtract(
                                    summary.getExpense()
                            )
                    )
            );
        }

        return response;
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
                    recurringTransactionRepository.findActiveForHouseholdPeriod(
                            user.getHousehold().getId(),
                            periodStart,
                            periodEnd
                    );

            BigDecimal projectedIncome =
                    calculateRecurringAmount(
                            recurringTransactions,
                            TransactionType.INCOME,
                            periodStart,
                            periodEnd
                    );

            BigDecimal projectedRecurringExpense =
                    calculateRecurringAmount(
                            recurringTransactions,
                            TransactionType.EXPENSE,
                            periodStart,
                            periodEnd
                    );

            List<CreditCardInstallment> installments =
                    installmentRepository
                            .findByHouseholdInvoice(
                                    user.getHousehold().getId(),
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

    @Override
    @Transactional(readOnly = true)
    public List<CashFlowResponse> getCashFlow(
            Integer year
    ) {

        User user = currentUserService.getAuthenticatedUser();

        Map<Month, MonthlySummaryProjection> monthlySummary =
                getHouseholdMonthlySummary(
                        user.getHousehold().getId(),
                        year
                );

        return completeMonthlySeries(monthlySummary);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CumulativeResultResponse> getCumulativeResult(
            Integer year
    ) {

        User user = currentUserService.getAuthenticatedUser();

        Map<Month, MonthlySummaryProjection> monthlySummary =
                getHouseholdMonthlySummary(
                        user.getHousehold().getId(),
                        year
                );

        return buildCumulativeResult(monthlySummary);
    }

    @Override
    @Transactional(readOnly = true)
    public IncomeCommitmentResponse getIncomeCommitment() {

        User user = currentUserService.getAuthenticatedUser();

        UUID householdId = user.getHousehold().getId();

        LocalDate referenceDate = LocalDate.now();

        LocalDate periodStart =
                referenceDate.withDayOfMonth(1);

        LocalDate periodEnd =
                referenceDate.withDayOfMonth(
                        referenceDate.lengthOfMonth()
                );

        List<RecurringTransaction> recurringTransactions =
                recurringTransactionRepository.findActiveForHouseholdPeriod(
                        householdId,
                        periodStart,
                        periodEnd
                );

        BigDecimal monthlyIncome =
                calculateRecurringAmount(
                        recurringTransactions,
                        TransactionType.INCOME,
                        periodStart,
                        periodEnd
                );

        BigDecimal recurringExpenses =
                calculateRecurringAmount(
                        recurringTransactions,
                        TransactionType.EXPENSE,
                        periodStart,
                        periodEnd
                );

        BigDecimal unpaidCreditCardInstallments =
                installmentRepository
                        .sumUnpaidInstallmentsByHousehold(
                                householdId
                        );

        return toIncomeCommitmentResponse(
                monthlyIncome,
                recurringExpenses,
                unpaidCreditCardInstallments
        );
    }
}
