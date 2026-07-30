package org.jejuro.miraero.domain.goal.dto.response;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoalPossibilityResponse {
    private Long requiredMonthly;
    private Long availableMonthly;
    private boolean possible;
}
