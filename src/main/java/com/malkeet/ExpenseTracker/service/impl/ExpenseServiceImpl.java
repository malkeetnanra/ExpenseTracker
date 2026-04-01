package com.malkeet.ExpenseTracker.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.malkeet.ExpenseTracker.dto.request.ExpenseCreateRequest;
import com.malkeet.ExpenseTracker.dto.response.ExpenseResponse;
import com.malkeet.ExpenseTracker.entity.Expense;
import com.malkeet.ExpenseTracker.entity.User;
import com.malkeet.ExpenseTracker.repository.ExpenseRepository;
import com.malkeet.ExpenseTracker.repository.UserRepository;
import com.malkeet.ExpenseTracker.service.ExpenseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    @Override
    public ExpenseResponse addExpense(Long userId, ExpenseCreateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Expense expense = new Expense();
        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDate(request.getDate().atStartOfDay());
        expense.setUser(user);

        Expense saved = expenseRepository.save(expense);

        return mapToResponse(saved);
    }

    @Override
    public List<ExpenseResponse> getUserExpenses(Long userId) {

        return expenseRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .date(expense.getDate())
                .build();
    }
}
