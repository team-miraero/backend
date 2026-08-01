package org.jejuro.miraero.domain.goal.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.dto.request.GoalCreateRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalPossibilityRequest;
import org.jejuro.miraero.domain.goal.dto.response.GoalCreateResponse;
import org.jejuro.miraero.domain.goal.dto.response.GoalListResponse;
import org.jejuro.miraero.domain.goal.dto.response.GoalPossibilityResponse;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService{


    private final GoalMapper goalMapper;
    private final GoalAssetService goalAssetService;


    /**
     * 목표 실현 가능성을 조회한다.
     *
     * @param request 목표 실현 가능성 조회 요청 정보
     * @return 목표 실현 가능성 응답 정보
     */
    @Override
    public GoalPossibilityResponse checkPossibility(GoalPossibilityRequest request) {

        //TODO:여유 자금 계산 API 나오면 호출 만원단위일거임
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

        if (goalAmount == null || goalAmount <= 0) {
            throw new IllegalArgumentException("목표 금액은 0보다 커야 합니다.");
        }

        if (startAmount == null || startAmount < 0) {
            throw new IllegalArgumentException("시작 금액은 0 이상이어야 합니다.");
        }

        if (goalMonths == null || goalMonths <= 0) {
            throw new IllegalArgumentException("목표 기간은 1개월 이상이어야 합니다.");
        }

        if (startAmount >= goalAmount) {
            return 0L;
        }


        long remainingAmount= goalAmount - startAmount;

        long monthlyAmount = remainingAmount / goalMonths;

        return ((monthlyAmount+9999)/10000)*10000;
    }


    /**
     * 목표를 생성한다.
     *
     * @param userId 사용자 ID
     * @param request 목표 생성 요청 정보
     * @return 생성된 목표 ID
     */
    @Override
    @Transactional
    public GoalCreateResponse createGoal(Long userId, GoalCreateRequest request) {


        //1. 목표 생성
        LocalDate startDate = LocalDate.now();
        LocalDate goalDate = startDate
                .plusMonths(request.getGoalMonths())
                .with(TemporalAdjusters.lastDayOfMonth());

        Goal goal = Goal.builder()
                .userId(userId)
                .goalType(request.getGoalType())
                .goalName(request.getGoalName())
                .goalAmount(request.getGoalAmount())
                .startAmount(request.getStartAmount())
                .goalDate(goalDate)
                .startDate(startDate)
                .build();

        goalMapper.save(goal);


        //2. 목표 자산 연결
        goalAssetService.saveGoalAssets(
                goal.getGoalId(),
                request.getAssets()
        );


        return GoalCreateResponse.builder()
                .goalId(goal.getGoalId())
                .build();
    }

    /**
     * 사용자의 목표 목록을 조회한다.
     *
     * @param userId 사용자 ID
     * @return 사용자가 생성한 목표 목록
     */
    @Override
    public List<GoalListResponse> getGoalsByUserId(Long userId) {

        List<Goal> goals = goalMapper.findGoalsByUserId(userId);

        return goals.stream()
                .map(goal -> {
                    Long currentAmount = goalAssetService.calculateCurrentAmount(
                            goal.getGoalId()
                    );

                    Integer progressRate = calculateProgressRate(
                            currentAmount,
                            goal.getGoalAmount()
                    );

                    return GoalListResponse.from(
                            goal,
                            progressRate
                    );
                })
                .toList();
    }

    /**
     * 목표 진행률 계산 반올림처리
     */
    private Integer calculateProgressRate(Long currentAmount, Long goalAmount) {
        if (goalAmount == null || goalAmount == 0) {
            return 0;
        }

        if(currentAmount == null) {
            return 0;
        }

        return (int) Math.round(
                currentAmount * 100.0 / goalAmount
        );
    }
}
