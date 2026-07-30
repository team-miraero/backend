package org.jejuro.miraero.domain.transaction.controller;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseDashboardResponse;
import org.jejuro.miraero.domain.transaction.service.ExpenseAnalysisService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expense-analysis")
public class ExpenseAnalysisController {
    // TODO JWT 인증 연동 후 SecurityContext에서 로그인 사용자 ID 조회
    private static final Long TEST_USER_ID = 1L;
    private final ExpenseAnalysisService expenseAnalysisService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<ExpenseDashboardResponse>> getDashboard(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        return ResponseEntity.ok(ApiResponse.success(expenseAnalysisService.getDashboard(TEST_USER_ID, year, month)));
    }
}
