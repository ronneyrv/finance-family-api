package com.ronney.finance.controller;

import com.ronney.finance.dto.request.GoalRequest;
import com.ronney.finance.dto.response.GoalResponse;
import com.ronney.finance.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
public class GoalController {
    private final GoalService goalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GoalResponse create(
            @Valid
            @RequestBody
            GoalRequest request
    ) {
        return goalService.create(request);
    }

    @GetMapping
    public List<GoalResponse> findAll() {
        return goalService.findAll();
    }

    @GetMapping("/{id}")
    public GoalResponse findById(
            @PathVariable UUID id
    ) {
        return goalService.findById(id);
    }

    @PutMapping("/{id}")
    public GoalResponse update(
            @PathVariable UUID id,
            @Valid
            @RequestBody
            GoalRequest request
    ) {
        return goalService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id
    ) {
        goalService.delete(id);
    }
}
