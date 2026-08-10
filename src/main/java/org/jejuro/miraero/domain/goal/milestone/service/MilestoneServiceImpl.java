package org.jejuro.miraero.domain.goal.milestone.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.exception.GoalErrorCode;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.jejuro.miraero.domain.goal.milestone.domain.Milestone;
import org.jejuro.miraero.domain.goal.milestone.domain.MilestoneReport;
import org.jejuro.miraero.domain.goal.milestone.dto.response.MilestoneListResponse;
import org.jejuro.miraero.domain.goal.milestone.dto.response.MilestoneReportResponse;
import org.jejuro.miraero.domain.goal.milestone.dto.response.MilestoneResponse;
import org.jejuro.miraero.domain.goal.milestone.mapper.MilestoneMapper;
import org.jejuro.miraero.domain.goal.milestone.mapper.MilestoneReportMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MilestoneServiceImpl implements MilestoneService {

    private static final List<Integer> MILESTONE_PERCENTAGES =
            List.of(25, 50, 75, 100);

    private final GoalMapper goalMapper;
    private final MilestoneMapper milestoneMapper;
    private final MilestoneReportMapper milestoneReportMapper;
    private final MilestoneReportService milestoneReportService;

    @Override
    @Transactional(readOnly = true)
    public MilestoneListResponse getMilestones(
            Long goalId,
            Long userId
    ) {
        // 목표 소유권 확인
        Goal goal = goalMapper.findByIdAndUserId(userId, goalId);

        if (goal == null) {
            throw new BusinessException(GoalErrorCode.GOAL_NOT_FOUND);
        }

        List<Milestone> milestones =
                milestoneMapper.findByGoalId(goalId);

        if(milestones.isEmpty()){
            return MilestoneListResponse.of(List.of());
        }

        List<Long> milestoneIds = milestones.stream()
                .map(Milestone::getMilestoneId)
                .toList();

        List<MilestoneReport> reports =
                milestoneReportMapper.findByMilestoneIds(milestoneIds);

        Map<Long, MilestoneReport> reportMap =
                reports.stream()
                        .collect(Collectors.toMap(
                                MilestoneReport::getMilestoneId,
                                report -> report
                        ));

        List<MilestoneResponse> responses =
                milestones.stream()
                        .map(milestone ->
                                toResponse(
                                        milestone,
                                        reportMap.get(milestone.getMilestoneId())
                                )
                        )
                        .toList();

        return MilestoneListResponse.of(responses);
    }

    @Override
    @Transactional
    public void createMilestones(
            Long goalId,
            Long goalAmount
    ) {
        List<Milestone> milestones =
                createMilestoneList(goalId, goalAmount);

        milestoneMapper.saveAll(milestones);
    }

    @Override
    @Transactional
    public void recreateMilestones(Long goalId, Long goalAmount) {
        milestoneMapper.deleteByGoalId(goalId);
        createMilestones(goalId, goalAmount);
    }

    @Override
    @Transactional
    public void updatedMilestoneAchievement(
            Long goalId,
            Long currentAmount
    ) {

        List<Milestone> milestones =
                milestoneMapper.findByGoalId(goalId);

        for (Milestone milestone : milestones) {

            if(!milestone.achieveIfReached(currentAmount)) continue;

            int updated = milestoneMapper.updateAchievement(milestone);

            if (updated == 0) {
                continue;
            }

            milestoneReportService.generateReport(
                    milestone.getMilestoneId(),
                    goalId
            );
        }
    }

    /**
     * 목표 금액을 기준으로
     * 25%, 50%, 75%, 100% 마일스톤을 생성한다.
     */
    private List<Milestone> createMilestoneList(
            Long goalId,
            Long goalAmount
    ) {
        return MILESTONE_PERCENTAGES.stream()
                .map(percentage ->
                        Milestone.builder()
                                .goalId(goalId)
                                .milestonePercentage(percentage)
                                .milestoneAmount(
                                        goalAmount * percentage / 100
                                )
                                .achieved(false)
                                .build()
                )
                .toList();
    }


    private MilestoneResponse toResponse(
            Milestone milestone,
            MilestoneReport report
    ) {
        return MilestoneResponse.from(
                milestone.getStep(),
                milestone,
                MilestoneReportResponse.from(report)
        );
    }

}