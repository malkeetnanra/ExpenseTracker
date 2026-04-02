package com.malkeet.ExpenseTracker.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.malkeet.ExpenseTracker.dto.request.LogoutRequest;
import com.malkeet.ExpenseTracker.dto.request.LoginRequest;
import com.malkeet.ExpenseTracker.dto.request.RefreshTokenRequest;
import com.malkeet.ExpenseTracker.dto.request.RegisterRequest;
import com.malkeet.ExpenseTracker.dto.response.AuthResponse;
import com.malkeet.ExpenseTracker.dto.response.MessageResponse;
import com.malkeet.ExpenseTracker.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/logout")
    public MessageResponse logout(@Valid @RequestBody LogoutRequest request) {
        return authService.logout(request);
    }
}
