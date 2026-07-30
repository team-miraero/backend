package org.jejuro.miraero.domain.goal.service;

import org.jejuro.miraero.domain.goal.dto.request.GoalPossibilityRequest;
import org.jejuro.miraero.domain.goal.dto.response.GoalPossibilityResponse;

public interface GoalService {
    GoalPossibilityResponse checkPossibility(
        GoalPossibilityRequest request
    );


}
