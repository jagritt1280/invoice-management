package com.invoice.service;

import com.invoice.dto.ExpenseRequest;
import com.invoice.dto.ExpenseResponse;
import com.invoice.entity.Expense;
import com.invoice.entity.User;
import com.invoice.exception.ResourceNotFoundException;
import com.invoice.repository.ExpenseRepository;
import com.invoice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final DashboardService dashboardService;
    @Transactional
    public ExpenseResponse create(ExpenseRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", 0L));

        Expense expense = Expense.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .amount(request.getAmount())
                .date(request.getDate())
                .notes(request.getNotes())
                .user(user)
                .build();

        Expense saved = expenseRepository.save(expense);
        dashboardService.evictDashboardCache(userEmail);
        log.info("Expense created: {} for user: {}", saved.getId(), userEmail);
        return mapToResponse(saved);
    }

    public List<ExpenseResponse> getAllByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", 0L));

        return expenseRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long id, String userEmail) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));

        if(!expense.getUser().getEmail().equals(userEmail))
            throw new ResourceNotFoundException("Expense", id);

        expenseRepository.delete(expense);
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .category(expense.getCategory())
                .amount(expense.getAmount())
                .date(expense.getDate())
                .notes(expense.getNotes())
                .build();
    }
}