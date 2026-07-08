package com.ronney.finance.service.impl;

import com.ronney.finance.domain.entity.CreditCardInstallment;
import com.ronney.finance.domain.entity.RecurringTransaction;
import com.ronney.finance.domain.entity.User;
import com.ronney.finance.domain.enums.TransactionType;
import com.ronney.finance.dto.response.CategoryExpenseResponse;
import com.ronney.finance.dto.response.DashboardSummaryResponse;
import com.ronney.finance.dto.response.MonthlyProjectionResponse;
import com.ronney.finance.dto.response.MonthlySummaryResponse;
import com.ronney.finance.repository.CreditCardInstallmentRepository;
import com.ronney.finance.repository.RecurringTransactionRepository;
import com.ronney.finance.repository.TransactionRepository;
import com.ronney.finance.service.CurrentUserService;
import com.ronney.finance.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final CreditCardInstallmentRepository installmentRepository;

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

        BigDecimal totalIcome = transactionRepository.sumByUserIdAndType(
                user.getId(),
                TransactionType.INCOME
        );

        BigDecimal totalExpense = transactionRepository.sumByUserIdAndType(
                user.getId(),
                TransactionType.EXPENSE
        );

        BigDecimal balance = totalIcome.subtract(
                totalExpense
        );

        return new DashboardSummaryResponse(
                totalIcome,
                totalExpense,
                balance
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
}
