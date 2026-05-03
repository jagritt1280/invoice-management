package com.invoice.repository;

import com.invoice.entity.Expense;
import com.invoice.entity.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserId(Long userId);

    List<Expense> findByUserIdAndCategory(Long userId, ExpenseCategory category);

    // total expenses for a user
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId")
    BigDecimal getTotalExpenses(@Param("userId") Long userId);

    // monthly expense breakdown
    @Query("SELECT MONTH(e.date), SUM(e.amount) FROM Expense e WHERE e.user.id = :userId GROUP BY MONTH(e.date)")
    List<Object[]> getMonthlyExpenses(@Param("userId") Long userId);
}
