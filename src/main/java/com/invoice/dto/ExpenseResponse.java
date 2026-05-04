package com.invoice.dto;

import com.invoice.entity.ExpenseCategory;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ExpenseResponse {

    private Long id;
    private String title;
    private ExpenseCategory category;
    private BigDecimal amount;
    private LocalDate date;
    private String notes;
}