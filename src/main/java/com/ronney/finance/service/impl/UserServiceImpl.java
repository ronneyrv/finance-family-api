package com.ronney.finance.service.impl;

import com.ronney.finance.domain.entity.User;
import com.ronney.finance.dto.request.UpdateCurrentUserRequest;
import com.ronney.finance.dto.response.CurrentUserResponse;
import com.ronney.finance.exception.BusinessException;
import com.ronney.finance.repository.UserRepository;
import com.ronney.finance.service.CurrentUserService;
import com.ronney.finance.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    @Override
    public CurrentUserResponse getCurrentUser() {

        User user = currentUserService.getAuthenticatedUser();

        return toResponse(user);
    }

    @Override
    public CurrentUserResponse updateCurrentUser(UpdateCurrentUserRequest request) {

        User user = currentUserService.getAuthenticatedUser();

        if (!user.getEmail().equals(request.email())
                && userRepository.existsByEmail(request.email())) {

            throw new BusinessException("Email is already in use.");
        }

        user.setName(request.name());
        user.setEmail(request.email());

        user = userRepository.save(user);

        return toResponse(user);
    }

    private CurrentUserResponse toResponse(
            User user
    ) {

        return new CurrentUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}