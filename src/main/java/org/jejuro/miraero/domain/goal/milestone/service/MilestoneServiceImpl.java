package org.jejuro.miraero.domain.goal.milestone.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.jejuro.miraero.domain.goal.milestone.domain.Milestone;
import org.jejuro.miraero.domain.goal.milestone.domain.MilestoneReport;
import org.jejuro.miraero.domain.goal.milestone.dto.response.MilestoneListResponse;
import org.jejuro.miraero.domain.goal.milestone.dto.response.MilestoneReportResponse;
import org.jejuro.miraero.domain.goal.milestone.dto.response.MilestoneResponse;
import org.jejuro.miraero.domain.goal.milestone.mapper.MilestoneMapper;
import org.jejuro.miraero.domain.goal.milestone.mapper.MilestoneReportMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MilestoneServiceImpl implements MilestoneService {

    private final GoalMapper goalMapper;
    private final MilestoneMapper milestoneMapper;
    private final MilestoneReportMapper milestoneReportMapper;

    @Override
    @Transactional(readOnly = true)
    public MilestoneListResponse getMilestones(
            Long goalId,
            Long userId
    ) {
        /*
         * 사용자의 목표인지 확인한다.
         */
        Goal goal = goalMapper.findByIdAndUserId(goalId, userId);

        if (goal == null) {
            throw new IllegalArgumentException("존재하지 않는 목표입니다.");
        }

        /*
         * 목표에 속한 마일스톤을 조회한다.
         *
         * DB에서 milestone_percentage ASC로 정렬한다.
         */
        List<Milestone> milestones =
                milestoneMapper.findByGoalId(goalId);

        List<MilestoneResponse> responses =
                milestones.stream()
                        .map(this::toResponse)
                        .toList();

        return MilestoneListResponse.of(responses);
    }

    /**
     * 마일스톤과 해당 마일스톤의 리포트를
     * 화면 응답 DTO로 변환한다.
     */
    private MilestoneResponse toResponse(
            Milestone milestone
    ) {
        MilestoneReport report =
                milestoneReportMapper.findByMilestoneId(
                        milestone.getMilestoneId()
                );

        return MilestoneResponse.from(
                getStep(milestone.getMilestonePercentage()),
                milestone,
                MilestoneReportResponse.from(report)
        );
    }

    /**
     * 마일스톤 퍼센트를 화면상의 단계로 변환한다.
     *
     * 25%  → 1단계
     * 50%  → 2단계
     * 75%  → 3단계
     * 100% → 4단계
     */
    private int getStep(int percentage) {
        return switch (percentage) {
            case 25 -> 1;
            case 50 -> 2;
            case 75 -> 3;
            case 100 -> 4;
            default -> throw new IllegalArgumentException(
                    "올바르지 않은 마일스톤 비율입니다."
            );
        };
    }
}