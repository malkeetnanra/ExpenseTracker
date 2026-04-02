package com.malkeet.ExpenseTracker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.malkeet.ExpenseTracker.entity.User;
import com.malkeet.ExpenseTracker.entity.enums.Role;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    long countByRole(Role role);
}
