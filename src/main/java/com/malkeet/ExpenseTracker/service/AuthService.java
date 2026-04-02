package com.malkeet.ExpenseTracker.service;

import com.malkeet.ExpenseTracker.dto.request.LogoutRequest;
import com.malkeet.ExpenseTracker.dto.request.LoginRequest;
import com.malkeet.ExpenseTracker.dto.request.RefreshTokenRequest;
import com.malkeet.ExpenseTracker.dto.request.RegisterRequest;
import com.malkeet.ExpenseTracker.dto.response.AuthResponse;
import com.malkeet.ExpenseTracker.dto.response.MessageResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    MessageResponse logout(LogoutRequest request);
}
