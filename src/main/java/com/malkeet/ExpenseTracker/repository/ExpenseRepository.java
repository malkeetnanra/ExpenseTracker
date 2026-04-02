package com.malkeet.ExpenseTracker.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.malkeet.ExpenseTracker.entity.Expense;
import com.malkeet.ExpenseTracker.entity.enums.ExpenseCategory;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    List<Expense> findByUserId(Long userId);

    List<Expense> findByUserIdAndCategory(Long userId, ExpenseCategory category);

    List<Expense> findByUserIdAndDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    List<Expense> findByUserIdAndCategoryAndDateBetween(
            Long userId,
            ExpenseCategory category,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    Optional<Expense> findByIdAndUserId(Long id, Long userId);
}
