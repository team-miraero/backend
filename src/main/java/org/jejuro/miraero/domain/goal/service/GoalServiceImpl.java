package org.jejuro.miraero.domain.goal.service;

import org.jejuro.miraero.domain.goal.dto.request.GoalPossibilityRequest;
import org.jejuro.miraero.domain.goal.dto.response.GoalPossibilityResponse;
import org.springframework.stereotype.Service;

@Service
public class GoalServiceImpl implements GoalService{


    @Override
    public GoalPossibilityResponse checkPossibility(GoalPossibilityRequest request) {

        // 여유 자금 계산 API 나오면 호출 만원단위일거임
        Long availableMonthly = 500000L;

        long requiredMonthly = calculateRequiredMonthly(
                request.getGoalAmount(),
                request.getStartAmount(),
                request.getGoalMonths()
        );

        boolean possible = availableMonthly >= requiredMonthly;


        return GoalPossibilityResponse.builder()
                .availableMonthly(availableMonthly)
                .requiredMonthly(requiredMonthly)
                .possible(possible)
                .build();
    }


    private Long calculateRequiredMonthly(
            Long goalAmount,
            Long startAmount,
            Integer goalMonths
    ){

        long remainingAmount= goalAmount - startAmount;

        long monthlyAmount = remainingAmount / goalMonths;

        return ((monthlyAmount+9999)/10000)*10000;
    }
}
