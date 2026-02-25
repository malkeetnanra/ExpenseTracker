package com.malkeet.ExpenseTracker.service;

import com.malkeet.ExpenseTracker.dto.request.LoginRequest;
import com.malkeet.ExpenseTracker.dto.request.RegisterRequest;
import com.malkeet.ExpenseTracker.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
