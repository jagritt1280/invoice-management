package com.invoice.controller;

import com.invoice.dto.ApiResponse;
import com.invoice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(
            Principal principal) {

        Map<String, Object> dashboard = dashboardService
                .getDashboard(principal.getName());

        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}