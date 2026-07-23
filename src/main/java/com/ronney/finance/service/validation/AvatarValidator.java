package com.ronney.finance.service.validation;

import com.ronney.finance.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Component
public class AvatarValidator {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    public void validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BusinessException(
                    "Avatar file must not be empty.",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(
                    "Avatar image must not exceed 5 MB.",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BusinessException(
                    "Only JPEG, PNG and WEBP images are allowed.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}