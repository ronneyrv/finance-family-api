package com.ronney.finance.service;

import com.ronney.finance.dto.request.UpdateCurrentUserRequest;
import com.ronney.finance.dto.response.CurrentUserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    CurrentUserResponse getCurrentUser();

    CurrentUserResponse updateCurrentUser(
            UpdateCurrentUserRequest request
    );

    CurrentUserResponse uploadAvatar(MultipartFile file);
}