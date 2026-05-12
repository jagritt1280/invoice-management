package com.invoice.service;

import com.invoice.entity.InvoiceStatus;
import com.invoice.entity.User;
import com.invoice.exception.ResourceNotFoundException;
import com.invoice.repository.ExpenseRepository;
import com.invoice.repository.InvoiceRepository;
import com.invoice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final InvoiceRepository invoiceRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    @Cacheable(value = "dashboard", key = "#userEmail")
    public Map<String, Object> getDashboard(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", 0L));

        Long userId = user.getId();

        // all these queries run in parallel in production with async
        // for now sequential — still fast with proper DB indexes

        BigDecimal totalRevenue  = invoiceRepository.getTotalRevenue(userId);
        BigDecimal totalExpenses = expenseRepository.getTotalExpenses(userId);

        // net P&L = revenue - expenses
        BigDecimal netPL = totalRevenue.subtract(totalExpenses);

        // overdue count — how many invoices are overdue
        Long overdueCount = invoiceRepository.countByUserIdAndStatus(
                userId, InvoiceStatus.OVERDUE);

        // paid count
        Long paidCount = invoiceRepository.countByUserIdAndStatus(
                userId, InvoiceStatus.PAID);

        // pending count (sent but not paid)
        Long pendingCount = invoiceRepository.countByUserIdAndStatus(
                userId, InvoiceStatus.SENT);

        // monthly revenue breakdown for chart
        // raw result: [[1, 50000], [2, 65000], [3, 48000]]
        // month number → revenue amount
        List<Object[]> monthlyRaw = invoiceRepository
                .getMonthlyRevenue(userId);

        Map<String, BigDecimal> monthlyRevenue = new HashMap<>();
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun",
                "Jul","Aug","Sep","Oct","Nov","Dec"};

        for(Object[] row : monthlyRaw) {
            int monthNum = ((Number) row[0]).intValue();
            BigDecimal amount = (BigDecimal) row[1];
            monthlyRevenue.put(months[monthNum - 1], amount);
        }

        // build response map
        // why Map<String, Object> and not a DTO?
        // dashboard response is flexible — might add/remove fields
        // Map is easier to extend than creating new DTO every time
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalRevenue",   totalRevenue);
        dashboard.put("totalExpenses",  totalExpenses);
        dashboard.put("netProfitLoss",  netPL);
        dashboard.put("overdueCount",   overdueCount);
        dashboard.put("paidCount",      paidCount);
        dashboard.put("pendingCount",   pendingCount);
        dashboard.put("monthlyRevenue", monthlyRevenue);

        return dashboard;
    }

    @CacheEvict(value = "dashboard", key = "#userEmail")
    // ↑ clears cache when called
    // must be called whenever invoice/expense changes
    public void evictDashboardCache(String userEmail) {
        // empty method — just evicts cache ✅
    }


}