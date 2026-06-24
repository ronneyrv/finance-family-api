package com.ronney.finance.service.impl;

import com.ronney.finance.domain.entity.Goal;
import com.ronney.finance.domain.entity.User;
import com.ronney.finance.dto.request.GoalRequest;
import com.ronney.finance.dto.response.GoalResponse;
import com.ronney.finance.exception.ResourceNotFoundException;
import com.ronney.finance.repository.GoalRepository;
import com.ronney.finance.service.CurrentUserService;
import com.ronney.finance.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.PublicKey;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {
    private final GoalRepository goalRepository;
    private final CurrentUserService currentUserService;

    @Override
    public GoalResponse create(
            GoalRequest request
    ) {
        User user = currentUserService.getAuthenticatedUser();

        Goal goal = Goal.builder()
                .id(UUID.randomUUID())
                .name(request.name())
                .targetAmount(request.targetAmount())
                .currentAmount(BigDecimal.ZERO)
                .targetDate(request.targetDate())
                .user(user)
                .build();
        goal = goalRepository.save(goal);

        return toResponse(goal);
    }

    @Override
    public List<GoalResponse> findAll() {
        User user = currentUserService.getAuthenticatedUser();

        return goalRepository.findByUserId(
                        user.getId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public GoalResponse findById(
            UUID id
    ) {
        User user = currentUserService.getAuthenticatedUser();

        Goal goal = goalRepository.findByIdAndUserId(
                                id,
                                user.getId()
                        )
                        .orElseThrow(() -> new ResourceNotFoundException("Goal not found.")
                        );

        return toResponse(goal);
    }

    @Override
    public GoalResponse update(
            UUID id,
            GoalRequest request
    ) {
        User user = currentUserService.getAuthenticatedUser();

        Goal goal = goalRepository.findByIdAndUserId(
                                id,
                                user.getId()
                        )
                        .orElseThrow(() -> new ResourceNotFoundException("Goal not found.")
                        );

        goal.setName(request.name());

        goal.setTargetAmount(request.targetAmount());

        goal.setTargetDate(request.targetDate());

        goal = goalRepository.save(goal);

        return toResponse(goal);
    }

    @Override
    public void delete(
            UUID id
    ) {
        User user = currentUserService.getAuthenticatedUser();

        Goal goal = goalRepository.findByIdAndUserId(
                                id,
                                user.getId()
                        )
                        .orElseThrow(() -> new ResourceNotFoundException("Goal not found.")
                        );

        goalRepository.delete(goal);
    }

    private GoalResponse toResponse(Goal goal) {
        BigDecimal remainingAmount = goal.getTargetAmount()
                .subtract(goal.getCurrentAmount()
                );
        Integer progress = 0;

        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progress = goal.getCurrentAmount()
                    .multiply(BigDecimal.valueOf(100))
                            .divide(goal.getTargetAmount(), 0, RoundingMode.HALF_UP)
                            .intValue();
        }

        return new GoalResponse(
                goal.getId(),
                goal.getName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                remainingAmount,
                progress,
                goal.getTargetDate()
        );
    }
}
