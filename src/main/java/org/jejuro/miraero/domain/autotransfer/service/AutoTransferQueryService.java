package org.jejuro.miraero.domain.autotransfer.service;

public interface AutoTransferQueryService {
    /**
     * 목표에 설정된 자동이체 금액 합계를 조회한다.
     *
     * @param goalId 목표 ID
     * @return 목표 자동이체 금액 합계
     */
    Long getTargetGoalTransferAmount(
            Long goalId
    );

    /**
     * 사용자의 다른 목표에 설정된 자동이체 금액 합계를 조회한다.
     *
     * @param userId 사용자 ID
     * @param goalId 제외할 목표 ID
     * @return 다른 목표 자동이체 금액 합계
     */
    Long getOtherGoalTransferAmount(
            Long userId,
            Long goalId
    );
}
