package com.malkeet.ExpenseTracker.service.impl;

import com.malkeet.ExpenseTracker.dto.request.LoginRequest;
import com.malkeet.ExpenseTracker.dto.request.RegisterRequest;
import com.malkeet.ExpenseTracker.dto.response.AuthResponse;
import com.malkeet.ExpenseTracker.dto.response.UserResponse;
import com.malkeet.ExpenseTracker.entity.User;
import com.malkeet.ExpenseTracker.repository.UserRepository;
import com.malkeet.ExpenseTracker.service.AuthService;
import com.malkeet.ExpenseTracker.exception.BadRequestException;  // Assuming you create this exception class
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate if the username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        // Validate if the email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        // Validate password match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        // Create the User entity
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        // Save the user and create response
        User savedUser = userRepository.save(user);

        // Build AuthResponse
        return AuthResponse.builder()
                .message("User registered successfully")
                .user(UserResponse.builder()
                        .id(savedUser.getId())
                        .username(savedUser.getUsername())
                        .email(savedUser.getEmail())
                        .build())
                .token(null) // Add JWT token here once implemented
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Fetch user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        // Check if the password matches
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid username or password");
        }

        // Build AuthResponse
        return AuthResponse.builder()
                .message("Login successful")
                .user(UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build())
                .token(null) // Add JWT token here once implemented
                .build();
    }

}