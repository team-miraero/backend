package org.jejuro.miraero.domain.transaction.service;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionQueryService {
    /**
     * 사용자의 최근 급여 입금 일시를 조회한다.
     *
     * @param userId 사용자 ID
     * @return 가장 최근 급여 거래 일시
     */
    List<LocalDateTime> getLatestSalaryDateTimes(Long userId, int limit);

    /**
     * 특정 기간 내 고정 지출 금액 합계를 조회한다
     *
     * @param userId 사용자 ID
     * @param startDateTime 조회 시작 일시
     * @param endDateTime 조회 종료 일시
     * @return 고정 지출 합계 금액
     */
    Long getFixedExpenseSum(
            Long userId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    /**
     * 특정 기간 내 변동 지출 금액 합계를 조회한다.
     *
     * @param userId 사용자 ID
     * @param startDateTime 조회 시작 일시
     * @param endDateTime 조회 종료 일시
     * @return 변동 지출 합계 금액
     */
    Long getVariableExpenseSum(
            Long userId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    /**
     * 사용자의 오늘 지출 금액 합계를 조회한다.
     *
     * @param userId 사용자 ID
     * @return 오늘 지출 합계 금액
     */
    Long getTodayExpenseSum(Long userId);
}
