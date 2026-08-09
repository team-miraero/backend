package org.jejuro.miraero.domain.goal.milestone.service;

import org.jejuro.miraero.domain.goal.milestone.dto.response.MilestoneListResponse;

public interface MilestoneService {

    /**
     * 목표의 마일스톤 여정을 조회한다.
     *
     * @param goalId 목표 ID
     * @param userId 사용자 ID
     * @return 25%, 50%, 75%, 100% 마일스톤 및 AI 리포트
     */
    MilestoneListResponse getMilestones(Long goalId, Long userId);
}