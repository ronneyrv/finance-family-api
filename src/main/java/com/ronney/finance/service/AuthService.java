package com.ronney.finance.service;

import com.ronney.finance.domain.entity.Household;
import com.ronney.finance.domain.entity.RefreshToken;
import com.ronney.finance.domain.entity.User;
import com.ronney.finance.dto.request.LoginRequest;
import com.ronney.finance.dto.request.RefreshTokenRequest;
import com.ronney.finance.dto.request.RegisterRequest;
import com.ronney.finance.dto.response.LoginResponse;
import com.ronney.finance.dto.response.RefreshTokenResponse;
import com.ronney.finance.dto.response.RegisterResponse;
import com.ronney.finance.exception.BusinessException;
import com.ronney.finance.repository.HouseholdRepository;
import com.ronney.finance.repository.UserRepository;
import com.ronney.finance.security.CustomUserDetails;
import com.ronney.finance.security.CustomUserDetailsService;
import com.ronney.finance.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String ACCOUNT_CREATED_MESSAGE = "Account created successfully.";

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    // Authentication

    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        CustomUserDetails user =
                (CustomUserDetails) userDetailsService
                        .loadUserByUsername(request.email());

        String accessToken = jwtService.generateToken(user);

        String refreshToken = refreshTokenService
                .create(user.getUser())
                .getToken();

        return new LoginResponse(
                accessToken,
                refreshToken
        );
    }

    // Registration

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email is already registered.");
        }

        Household household = householdRepository.save(
                Household.builder()
                        .id(UUID.randomUUID())
                        .name(request.householdName())
                        .build()
        );

        User user = userRepository.save(
                User.builder()
                        .id(UUID.randomUUID())
                        .name(request.name())
                        .email(request.email())
                        .password(passwordEncoder.encode(request.password()))
                        .household(household)
                        .build()
        );

        return new RegisterResponse(ACCOUNT_CREATED_MESSAGE);
    }

    public RefreshTokenResponse refresh(
            RefreshTokenRequest request
    ) {

        RefreshToken refreshToken =
                refreshTokenService.findValidToken(
                        request.refreshToken()
                );

        UserDetails user =
                userDetailsService.loadUserByUsername(
                        refreshToken.getUser().getEmail()
                );

        String accessToken =
                jwtService.generateToken(user);

        return new RefreshTokenResponse(
                accessToken
        );
    }
}