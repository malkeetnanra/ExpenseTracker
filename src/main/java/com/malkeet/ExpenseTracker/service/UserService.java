package com.malkeet.ExpenseTracker.service;

import java.util.List;

import com.malkeet.ExpenseTracker.dto.response.UserResponseDTO;

public interface UserService {
    List<UserResponseDTO> getAllUsers();
}
