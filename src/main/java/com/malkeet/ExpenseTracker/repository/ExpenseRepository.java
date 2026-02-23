package com.malkeet.ExpenseTracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.malkeet.ExpenseTracker.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
}
