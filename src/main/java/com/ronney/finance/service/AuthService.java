package com.ronney.finance.service;

import com.ronney.finance.dto.request.LoginRequest;
import com.ronney.finance.dto.response.LoginResponse;
import com.ronney.finance.security.CustomUserDetailsService;
import com.ronney.finance.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetails user = userDetailsService.loadUserByUsername( request.email());

        String token = jwtService.generateToken(user);

        return new LoginResponse(token);
    }
}
