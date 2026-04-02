package com.malkeet.ExpenseTracker.controller;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.malkeet.ExpenseTracker.dto.request.ExpenseCreateRequest;
import com.malkeet.ExpenseTracker.dto.request.ExpenseUpdateRequest;
import com.malkeet.ExpenseTracker.dto.response.ExpenseResponse;
import com.malkeet.ExpenseTracker.dto.response.MonthlySummaryResponse;
import com.malkeet.ExpenseTracker.dto.response.PagedResponse;
import com.malkeet.ExpenseTracker.entity.enums.ExpenseCategory;
import com.malkeet.ExpenseTracker.service.ExpenseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse createExpense(@Valid @RequestBody ExpenseCreateRequest request, Principal principal) {
        return expenseService.addExpense(principal.getName(), request);
    }

    @GetMapping
    public PagedResponse<ExpenseResponse> getExpenses(
            @RequestParam(required = false) ExpenseCategory category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Principal principal
    ) {
        return expenseService.getUserExpenses(principal.getName(), category, startDate, endDate, page, size, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public ExpenseResponse getExpense(@PathVariable Long id, Principal principal) {
        return expenseService.getExpenseById(principal.getName(), id);
    }

    @PutMapping("/{id}")
    public ExpenseResponse updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseUpdateRequest request,
            Principal principal
    ) {
        return expenseService.updateExpense(principal.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@PathVariable Long id, Principal principal) {
        expenseService.deleteExpense(principal.getName(), id);
    }

    @GetMapping("/summary/monthly")
    public MonthlySummaryResponse getMonthlySummary(
            @RequestParam int year,
            @RequestParam int month,
            Principal principal
    ) {
        return expenseService.getMonthlySummary(principal.getName(), year, month);
    }
}
