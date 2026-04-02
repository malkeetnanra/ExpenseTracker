package com.malkeet.ExpenseTracker.dto.request;

import java.time.LocalDate;

import com.malkeet.ExpenseTracker.entity.enums.ExpenseCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseUpdateRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @Positive(message = "Amount must be positive")
    private double amount;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Category is required")
    private ExpenseCategory category;
}
