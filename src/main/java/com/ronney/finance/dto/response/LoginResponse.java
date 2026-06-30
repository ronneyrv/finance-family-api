package com.ronney.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(

        @Schema(description = "JWT access token")
        String accessToken
) {
}
