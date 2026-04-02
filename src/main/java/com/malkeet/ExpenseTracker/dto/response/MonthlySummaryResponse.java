package com.malkeet.ExpenseTracker.dto.response;

import java.math.BigDecimal;
import java.util.Map;

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
public class MonthlySummaryResponse {
    private String month;
    private BigDecimal totalAmount;
    private long totalExpenses;
    private Map<ExpenseCategory, BigDecimal> categoryBreakdown;

}
