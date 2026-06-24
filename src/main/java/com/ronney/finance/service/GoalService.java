package com.ronney.finance.service;

import com.ronney.finance.dto.request.GoalRequest;
import com.ronney.finance.dto.response.GoalResponse;

import java.util.List;
import java.util.UUID;

public interface GoalService {
    GoalResponse create(
            GoalRequest request
    );

    List<GoalResponse> findAll();

    GoalResponse findById(
            UUID id
    );

    GoalResponse update(
            UUID id,
            GoalRequest request
    );

    void delete(
            UUID id
    );
}
