package com.ronney.finance.service;

import com.ronney.finance.domain.entity.Household;
import com.ronney.finance.domain.entity.User;
import com.ronney.finance.dto.request.LoginRequest;
import com.ronney.finance.dto.request.RegisterRequest;
import com.ronney.finance.dto.response.LoginResponse;
import com.ronney.finance.dto.response.RegisterResponse;
import com.ronney.finance.exception.BusinessException;
import com.ronney.finance.repository.HouseholdRepository;
import com.ronney.finance.repository.UserRepository;
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

    // Authentication

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        UserDetails user = userDetailsService.loadUserByUsername(request.email());

        String token = jwtService.generateToken(user);

        return new LoginResponse(token);
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
}