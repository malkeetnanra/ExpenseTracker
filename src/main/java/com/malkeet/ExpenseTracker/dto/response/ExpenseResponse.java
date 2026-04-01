package com.malkeet.ExpenseTracker.dto.response;

import java.time.LocalDateTime;

import com.malkeet.ExpenseTracker.entity.enums.ExpenseCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    private Long id;
    private String title;
    private String description;
    private Double amount;
    private ExpenseCategory category;
    private LocalDateTime date;

}
