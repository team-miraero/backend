package org.jejuro.miraero.domain.goal.service;

import org.jejuro.miraero.domain.goal.dto.request.GoalCreateRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalPossibilityRequest;
import org.jejuro.miraero.domain.goal.dto.response.GoalCreateResponse;
import org.jejuro.miraero.domain.goal.dto.response.GoalDetailResponse;
import org.jejuro.miraero.domain.goal.dto.response.GoalListResponse;
import org.jejuro.miraero.domain.goal.dto.response.GoalPossibilityResponse;

import java.util.List;

public interface GoalService {
    GoalPossibilityResponse checkPossibility(
        GoalPossibilityRequest request
    );

    GoalCreateResponse createGoal(
        Long userId,
        GoalCreateRequest request
    );

    List<GoalListResponse> getGoalsByUserId(Long userId);

    GoalDetailResponse getGoalDetail(Long userId, Long goalId);

}
