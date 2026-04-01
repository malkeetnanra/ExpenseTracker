package com.malkeet.ExpenseTracker.service;

import java.util.List;

import com.malkeet.ExpenseTracker.dto.request.ExpenseCreateRequest;
import com.malkeet.ExpenseTracker.dto.response.ExpenseResponse;

public interface ExpenseService {

    ExpenseResponse addExpense(Long userId, ExpenseCreateRequest request);

    List<ExpenseResponse> getUserExpenses(Long userId);
}
