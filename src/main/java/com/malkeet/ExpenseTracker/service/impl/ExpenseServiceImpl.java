package com.malkeet.ExpenseTracker.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.malkeet.ExpenseTracker.dto.request.ExpenseCreateRequest;
import com.malkeet.ExpenseTracker.dto.request.ExpenseUpdateRequest;
import com.malkeet.ExpenseTracker.dto.response.ExpenseResponse;
import com.malkeet.ExpenseTracker.dto.response.MonthlySummaryResponse;
import com.malkeet.ExpenseTracker.entity.Expense;
import com.malkeet.ExpenseTracker.entity.User;
import com.malkeet.ExpenseTracker.entity.enums.ExpenseCategory;
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
    public ExpenseResponse addExpense(String userEmail, ExpenseCreateRequest request) {
        User user = getUserByEmail(userEmail);

        Expense expense = new Expense();
        applyExpenseDetails(expense, request.getTitle(), request.getDescription(), request.getAmount(),
                request.getCategory(), request.getDate());
        expense.setUser(user);

        return mapToResponse(expenseRepository.save(expense));
    }

    @Override
    public List<ExpenseResponse> getUserExpenses(
            String userEmail,
            ExpenseCategory category,
            LocalDate startDate,
            LocalDate endDate
    ) {
        User user = getUserByEmail(userEmail);
        LocalDate effectiveStartDate = startDate;
        LocalDate effectiveEndDate = endDate;

        if (effectiveStartDate != null && effectiveEndDate == null) {
            effectiveEndDate = effectiveStartDate;
        }
        if (effectiveEndDate != null && effectiveStartDate == null) {
            effectiveStartDate = effectiveEndDate;
        }
        if (effectiveStartDate != null && effectiveEndDate != null && effectiveEndDate.isBefore(effectiveStartDate)) {
            throw new IllegalArgumentException("endDate must be on or after startDate");
        }

        List<Expense> expenses;
        if (category != null && effectiveStartDate != null) {
            expenses = expenseRepository.findByUserIdAndCategoryAndDateBetween(
                    user.getId(),
                    category,
                    effectiveStartDate.atStartOfDay(),
                    effectiveEndDate.plusDays(1).atStartOfDay().minusNanos(1)
            );
        } else if (category != null) {
            expenses = expenseRepository.findByUserIdAndCategory(user.getId(), category);
        } else if (effectiveStartDate != null) {
            expenses = expenseRepository.findByUserIdAndDateBetween(
                    user.getId(),
                    effectiveStartDate.atStartOfDay(),
                    effectiveEndDate.plusDays(1).atStartOfDay().minusNanos(1)
            );
        } else {
            expenses = expenseRepository.findByUserId(user.getId());
        }

        return expenses.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ExpenseResponse getExpenseById(String userEmail, Long expenseId) {
        User user = getUserByEmail(userEmail);
        return mapToResponse(getExpenseForUser(user.getId(), expenseId));
    }

    @Override
    public ExpenseResponse updateExpense(String userEmail, Long expenseId, ExpenseUpdateRequest request) {
        User user = getUserByEmail(userEmail);
        Expense expense = getExpenseForUser(user.getId(), expenseId);

        applyExpenseDetails(expense, request.getTitle(), request.getDescription(), request.getAmount(),
                request.getCategory(), request.getDate());

        return mapToResponse(expenseRepository.save(expense));
    }

    @Override
    public void deleteExpense(String userEmail, Long expenseId) {
        User user = getUserByEmail(userEmail);
        Expense expense = getExpenseForUser(user.getId(), expenseId);
        expenseRepository.delete(expense);
    }

    @Override
    public MonthlySummaryResponse getMonthlySummary(String userEmail, int year, int month) {
        User user = getUserByEmail(userEmail);
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().plusDays(1).atStartOfDay().minusNanos(1);
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(user.getId(), start, end);

        Map<ExpenseCategory, BigDecimal> categoryBreakdown = new EnumMap<>(ExpenseCategory.class);
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Expense expense : expenses) {
            BigDecimal amount = BigDecimal.valueOf(expense.getAmount());
            totalAmount = totalAmount.add(amount);
            categoryBreakdown.merge(expense.getCategory(), amount, BigDecimal::add);
        }

        return MonthlySummaryResponse.builder()
                .month(yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                .totalAmount(totalAmount)
                .totalExpenses(expenses.size())
                .categoryBreakdown(categoryBreakdown)
                .build();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Expense getExpenseForUser(Long userId, Long expenseId) {
        return expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
    }

    private void applyExpenseDetails(
            Expense expense,
            String title,
            String description,
            double amount,
            ExpenseCategory category,
            LocalDate date
    ) {
        expense.setTitle(title);
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setCategory(category);
        expense.setDate(date.atStartOfDay());
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
