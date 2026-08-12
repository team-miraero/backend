package org.jejuro.miraero.domain.transaction.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseDashboardResponse;
import org.jejuro.miraero.domain.transaction.service.ExpenseAnalysisService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expense-analysis")
@Api(tags = "지출 분석")
public class ExpenseAnalysisController {
    private final ExpenseAnalysisService expenseAnalysisService;

    @GetMapping("/dashboard")
    @ApiOperation(value = "현재 기준 지출 분석 대시보드 조회", notes = "요청 파라미터 없이 서버의 현재 월을 기준으로 조회합니다. 최근 3개월 평균은 현재 월을 제외한 직전 3개월의 카테고리별 월평균이며, 또래 평균과 전월 대비 지출 변화는 현재 월을 사용합니다.")
    public ResponseEntity<ApiResponse<ExpenseDashboardResponse>> getDashboard(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        YearMonth currentMonth = YearMonth.now();
        return ResponseEntity.ok(ApiResponse.success(expenseAnalysisService.getDashboard(
                user.getUserId(),
                currentMonth.getYear(),
                currentMonth.getMonthValue()
        )));
    }
}
