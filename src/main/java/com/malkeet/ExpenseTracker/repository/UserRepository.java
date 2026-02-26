package com.malkeet.ExpenseTracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.malkeet.ExpenseTracker.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User>findByEmail(String email);
    boolean existsByUsername(String username);
}
