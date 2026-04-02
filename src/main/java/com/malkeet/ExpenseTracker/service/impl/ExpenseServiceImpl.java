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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.malkeet.ExpenseTracker.dto.request.ExpenseCreateRequest;
import com.malkeet.ExpenseTracker.dto.request.ExpenseUpdateRequest;
import com.malkeet.ExpenseTracker.dto.response.ExpenseResponse;
import com.malkeet.ExpenseTracker.dto.response.MonthlySummaryResponse;
import com.malkeet.ExpenseTracker.dto.response.PagedResponse;
import com.malkeet.ExpenseTracker.entity.Expense;
import com.malkeet.ExpenseTracker.entity.User;
import com.malkeet.ExpenseTracker.entity.enums.ExpenseCategory;
import com.malkeet.ExpenseTracker.exception.BadRequestException;
import com.malkeet.ExpenseTracker.exception.NotFoundException;
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
    public PagedResponse<ExpenseResponse> getUserExpenses(
            String userEmail,
            ExpenseCategory category,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size,
            String sortBy,
            String sortDir
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
            throw new BadRequestException("endDate must be on or after startDate");
        }

        Sort sort = Sort.by("asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Expense> specification = Specification.where(hasUserId(user.getId()));
        if (category != null) {
            specification = specification.and(hasCategory(category));
        }
        if (effectiveStartDate != null) {
            specification = specification.and(isWithinDateRange(effectiveStartDate, effectiveEndDate));
        }

        Page<Expense> expenses = expenseRepository.findAll(specification, pageable);

        return PagedResponse.<ExpenseResponse>builder()
                .content(expenses.getContent().stream().map(this::mapToResponse).toList())
                .page(expenses.getNumber())
                .size(expenses.getSize())
                .totalElements(expenses.getTotalElements())
                .totalPages(expenses.getTotalPages())
                .first(expenses.isFirst())
                .last(expenses.isLast())
                .build();
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
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private Expense getExpenseForUser(Long userId, Long expenseId) {
        return expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new NotFoundException("Expense not found"));
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

    private Specification<Expense> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    private Specification<Expense> hasCategory(ExpenseCategory category) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("category"), category);
    }

    private Specification<Expense> isWithinDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("date"), start, end);
    }
}
