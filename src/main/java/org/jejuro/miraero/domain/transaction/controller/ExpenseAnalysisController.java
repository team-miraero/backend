package org.jejuro.miraero.domain.transaction.controller;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseDashboardResponse;
import org.jejuro.miraero.domain.transaction.service.ExpenseAnalysisService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expense-analysis")
public class ExpenseAnalysisController {
    private final ExpenseAnalysisService expenseAnalysisService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<ExpenseDashboardResponse>> getDashboard(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(ApiResponse.success(expenseAnalysisService.getDashboard(user.getUserId(), year, month)));
    }
}
