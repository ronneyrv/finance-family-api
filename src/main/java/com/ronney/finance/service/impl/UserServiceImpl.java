package com.ronney.finance.service.impl;

import com.ronney.finance.domain.entity.User;
import com.ronney.finance.dto.request.UpdateCurrentUserRequest;
import com.ronney.finance.dto.response.CurrentUserResponse;
import com.ronney.finance.repository.UserRepository;
import com.ronney.finance.service.CurrentUserService;
import com.ronney.finance.service.StorageService;
import com.ronney.finance.service.UserService;
import com.ronney.finance.service.dto.StorageResult;
import com.ronney.finance.service.validation.AvatarValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final AvatarValidator avatarValidator;

    @Override
    public CurrentUserResponse getCurrentUser() {

        User user = currentUserService.getAuthenticatedUser();

        return toResponse(user);
    }

    @Override
    public CurrentUserResponse updateCurrentUser(
            UpdateCurrentUserRequest request
    ) {

        User user = currentUserService.getAuthenticatedUser();

        user.setName(request.name());

        user = userRepository.save(user);

        return toResponse(user);
    }

    @Override
    public CurrentUserResponse uploadAvatar(MultipartFile file) {

        avatarValidator.validate(file);

        User user = currentUserService.getAuthenticatedUser();

        if (user.getAvatarPublicId() != null) {
            storageService.delete(user.getAvatarPublicId());
        }

        StorageResult storageResult = storageService.upload(file);

        user.setAvatarUrl(storageResult.url());
        user.setAvatarPublicId(storageResult.publicId());

        user = userRepository.save(user);

        return toResponse(user);
    }

    private CurrentUserResponse toResponse(
            User user
    ) {

        return new CurrentUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAvatarUrl()
        );
    }
}