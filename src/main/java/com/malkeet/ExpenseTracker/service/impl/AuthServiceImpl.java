package com.malkeet.ExpenseTracker.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.malkeet.ExpenseTracker.dto.request.LogoutRequest;
import com.malkeet.ExpenseTracker.dto.request.LoginRequest;
import com.malkeet.ExpenseTracker.dto.request.RefreshTokenRequest;
import com.malkeet.ExpenseTracker.dto.request.RegisterRequest;
import com.malkeet.ExpenseTracker.dto.response.AuthResponse;
import com.malkeet.ExpenseTracker.dto.response.MessageResponse;
import com.malkeet.ExpenseTracker.entity.RefreshToken;
import com.malkeet.ExpenseTracker.entity.User;
import com.malkeet.ExpenseTracker.entity.enums.Role;
import com.malkeet.ExpenseTracker.exception.BadRequestException;
import com.malkeet.ExpenseTracker.exception.ConflictException;
import com.malkeet.ExpenseTracker.exception.NotFoundException;
import com.malkeet.ExpenseTracker.repository.RefreshTokenRepository;
import com.malkeet.ExpenseTracker.repository.UserRepository;
import com.malkeet.ExpenseTracker.security.JwtService;
import com.malkeet.ExpenseTracker.service.AuthService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 7;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(determineInitialRole())
                .build();

        User savedUser = userRepository.save(user);
        return buildAuthResponse(savedUser, createRefreshToken(savedUser));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        return buildAuthResponse(user, createRefreshToken(user));
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Refresh token has expired or was revoked");
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return buildAuthResponse(refreshToken.getUser(), createRefreshToken(refreshToken.getUser()));
    }

    @Override
    @Transactional
    public MessageResponse logout(LogoutRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return MessageResponse.builder()
                .message("Logged out successfully")
                .build();
    }

    private Role determineInitialRole() {
        return userRepository.countByRole(Role.ADMIN) == 0 ? Role.ADMIN : Role.USER;
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS))
                .revoked(false)
                .user(user)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    private AuthResponse buildAuthResponse(User user, RefreshToken refreshToken) {
        return AuthResponse.builder()
                .token(jwtService.generateToken(user.getEmail()))
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .role(user.getRole().name())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
