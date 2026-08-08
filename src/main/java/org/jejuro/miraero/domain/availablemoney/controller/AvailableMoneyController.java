package org.jejuro.miraero.domain.availablemoney.controller;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.availablemoney.dto.response.DailyAvailableMoneyResponse;
import org.jejuro.miraero.domain.availablemoney.dto.response.MonthlyAvailableMoneyResponse;
import org.jejuro.miraero.domain.availablemoney.service.AvailableMoneyService;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals/{goalId}/available-money")
public class AvailableMoneyController {

    private final AvailableMoneyService availableMoneyService;

    /**
     * 월 여유자금 조회
     */
    @GetMapping("/monthly")
    public ResponseEntity<MonthlyAvailableMoneyResponse> getMonthlyAvailableMoney(
            @PathVariable("goalId") Long goalId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Long userId = user.getUserId();
        MonthlyAvailableMoneyResponse response = availableMoneyService.getMonthlyAvailableMoney(userId, goalId);
        return ResponseEntity.ok(response);
    }

    /**
     * 일일 여유자금 및 오늘 남은 자금 조회
     */
    @GetMapping("/daily")
    public ResponseEntity<DailyAvailableMoneyResponse> getDailyAvailableMoney(
            @PathVariable("goalId") Long goalId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Long userId = user.getUserId();
        DailyAvailableMoneyResponse response = availableMoneyService.getDailyAvailableMoney(userId, goalId);
        return ResponseEntity.ok(response);
    }
}