package com.invoice.controller;

import com.invoice.dto.ApiResponse;
import com.invoice.dto.ExpenseRequest;
import com.invoice.dto.ExpenseResponse;
import com.invoice.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> create(
            @Valid @RequestBody ExpenseRequest request,
            Principal principal) {

        ExpenseResponse response = expenseService
                .create(request, principal.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Expense added successfully",
                        response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getAll(
            Principal principal) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        expenseService.getAllByUser(principal.getName())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            Principal principal) {

        expenseService.delete(id, principal.getName());

        return ResponseEntity.ok(
                ApiResponse.success("Expense deleted successfully"));
    }
}