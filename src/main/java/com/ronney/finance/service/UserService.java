package com.ronney.finance.service;

import com.ronney.finance.dto.request.UpdateCurrentUserRequest;
import com.ronney.finance.dto.response.CurrentUserResponse;

public interface UserService {

    CurrentUserResponse getCurrentUser();

    CurrentUserResponse updateCurrentUser(
            UpdateCurrentUserRequest request
    );
}