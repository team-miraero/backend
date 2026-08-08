package org.jejuro.miraero.domain.availablemoney.service;

import org.jejuro.miraero.domain.availablemoney.dto.response.DailyAvailableMoneyResponse;
import org.jejuro.miraero.domain.availablemoney.dto.response.MonthlyAvailableMoneyResponse;

public interface AvailableMoneyService {
    /**
     * 목표에 대한 월간 여유자금을 조회한다.
     *
     * @param userId 사용자 ID
     * @param goalId 여유자금을 계산할 목표 ID
     * @return 월간 여유자금 조회 결과
     */
    MonthlyAvailableMoneyResponse getMonthlyAvailableMoney(Long userId, Long goalId);


    /**
     * 목표에 대한 일간 여유자금을 조회한다.
     *
     * @param userId 사용자 ID
     * @param goalId 여유자금을 계산할 목표 ID
     * @return 일간 여유자금 조회 결과
     */
    DailyAvailableMoneyResponse getDailyAvailableMoney(Long userId, Long goalId);
}
