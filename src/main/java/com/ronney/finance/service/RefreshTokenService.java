package com.ronney.finance.service;

import com.ronney.finance.domain.entity.RefreshToken;
import com.ronney.finance.domain.entity.User;
import com.ronney.finance.exception.BusinessException;
import com.ronney.finance.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken create(User user) {

        Instant now = Instant.now();

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token(UUID.randomUUID().toString())
                .createdAt(now)
                .expiresAt(now.plusMillis(refreshTokenExpiration))
                .revoked(false)
                .user(user)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken findValidToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository.findByTokenWithUser(token)
                .orElseThrow(() ->
                        new BusinessException(
                                "Invalid refresh token.",
                                HttpStatus.UNAUTHORIZED
                        )
                );

        if (refreshToken.isRevoked()) {
            throw new BusinessException(
                    "Refresh token has been revoked.",
                    HttpStatus.UNAUTHORIZED
            );
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException(
                    "Refresh token has expired.",
                    HttpStatus.UNAUTHORIZED
            );
        }

        return refreshToken;
    }

    @Transactional
    public void revoke(RefreshToken refreshToken) {

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }
}