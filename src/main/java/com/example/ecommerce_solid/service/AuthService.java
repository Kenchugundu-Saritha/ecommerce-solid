package com.example.ecommerce_solid.service;

import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.ecommerce_solid.dto.LoginRequest;
import com.example.ecommerce_solid.dto.LoginResponse;
import com.example.ecommerce_solid.security.JwtService;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        String token =
                jwtService.generateToken(
                        authentication.getName()
                );

        return new LoginResponse(token);
    }
}