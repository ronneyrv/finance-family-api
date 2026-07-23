package com.ronney.finance.dto.response;

import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String name,
        String email,
        String avatarUrl
) {
}