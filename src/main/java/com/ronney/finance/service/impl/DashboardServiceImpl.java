package com.ronney.finance.service.impl;

import com.ronney.finance.domain.entity.User;
import com.ronney.finance.domain.enums.TransactionType;
import com.ronney.finance.dto.response.CategoryExpenseResponse;
import com.ronney.finance.dto.response.DashboardSummaryResponse;
import com.ronney.finance.repository.TransactionRepository;
import com.ronney.finance.service.CurrentUserService;
import com.ronney.finance.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

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
}
