package com.malkeet.ExpenseTracker.service;

import java.time.LocalDate;

import com.malkeet.ExpenseTracker.dto.request.ExpenseCreateRequest;
import com.malkeet.ExpenseTracker.dto.request.ExpenseUpdateRequest;
import com.malkeet.ExpenseTracker.dto.response.ExpenseResponse;
import com.malkeet.ExpenseTracker.dto.response.MonthlySummaryResponse;
import com.malkeet.ExpenseTracker.dto.response.PagedResponse;
import com.malkeet.ExpenseTracker.entity.enums.ExpenseCategory;

public interface ExpenseService {

    ExpenseResponse addExpense(String userEmail, ExpenseCreateRequest request);

    PagedResponse<ExpenseResponse> getUserExpenses(
            String userEmail,
            ExpenseCategory category,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size,
            String sortBy,
            String sortDir
    );

    ExpenseResponse getExpenseById(String userEmail, Long expenseId);

    ExpenseResponse updateExpense(String userEmail, Long expenseId, ExpenseUpdateRequest request);

    void deleteExpense(String userEmail, Long expenseId);

    MonthlySummaryResponse getMonthlySummary(String userEmail, int year, int month);
}
