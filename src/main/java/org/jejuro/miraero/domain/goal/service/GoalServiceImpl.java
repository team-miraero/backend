package org.jejuro.miraero.domain.goal.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.domain.PaceStatus;
import org.jejuro.miraero.domain.goal.dto.request.GoalCreateRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalPossibilityRequest;
import org.jejuro.miraero.domain.goal.dto.response.*;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.exception.GoalErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

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
    @Transactional(readOnly = true)
    public GoalPossibilityResponse checkPossibility(GoalPossibilityRequest request) {

        //TODO:여유 자금 계산 API 나오면 호출 만원단위일거임
        Long availableMonthly = 500000L;

        long requiredMonthly = calculateRequiredMonthlyDisplay(
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


    private Long calculateRequiredMonthlyDisplay(
            Long goalAmount,
            Long startAmount,
            Integer goalMonths
    ){
        validateGoalInput(goalAmount,startAmount,goalMonths);

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
    @Transactional(readOnly = true)
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

        return Math.min(100,(int) Math.round(
                currentAmount * 100.0 / goalAmount
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public GoalDetailResponse getGoalDetail(Long userId, Long goalId) {

        // 목표 기본 정보 조회
        Goal goal = goalMapper.findByIdAndUserId(userId,goalId);

        if(goal == null){
            throw new BusinessException(GoalErrorCode.GOAL_NOT_FOUND);
        }

        // 연결된 자산들의 현재 금액 계산
        Long currentAmount = Optional.ofNullable(
                goalAssetService.calculateCurrentAmount(goalId)
        ).orElse(0L);

        // 진행률 계산
        int progressRate = calculateProgressRate(
                currentAmount,
                goal.getGoalAmount()
        );

        // 기간 정보 생성
        GoalPeriodResponse period = GoalPeriodResponse.builder()
                .goalMonths(
                        (int) ChronoUnit.MONTHS.between(
                                YearMonth.from(goal.getStartDate()),
                                YearMonth.from(goal.getGoalDate())
                        )
                )
                .startDate(YearMonth.from(goal.getStartDate()))
                .endDate(YearMonth.from(goal.getGoalDate()))
                .remainMonths(
                        Math.max(0,(int) ChronoUnit.MONTHS.between(
                                YearMonth.now(),
                                YearMonth.from(goal.getGoalDate())
                        ))
                )
                .build();

        // 페이스 계산
        GoalPaceResponse pace = calculatePace(
                goal,
                currentAmount
        );

        return GoalDetailResponse.builder()
                .goalId(goal.getGoalId())
                .goalName(goal.getGoalName())
                .goalType(goal.getGoalType())
                .goalAmount(goal.getGoalAmount())
                .currentAmount(currentAmount)
                .startAmount(goal.getStartAmount())
                .progressRate(progressRate)
                .status(goal.getGoalStatus())
                .period(period)
                .pace(pace)
                .build();
    }

    private GoalPaceResponse calculatePace(
            Goal goal,
            Long currentAmount
    ) {

        long goalMonths = ChronoUnit.MONTHS.between(
                YearMonth.from(goal.getStartDate()),
                YearMonth.from(goal.getGoalDate())
        );

        long elapsedMonths = ChronoUnit.MONTHS.between(
                YearMonth.from(goal.getStartDate()),
                YearMonth.now()
        );

        long requiredMonthly = calculateRequiredMonthly(
                goal.getGoalAmount(),
                goal.getStartAmount(),
                goalMonths
        );


        long expectedAmount = goal.getStartAmount() + (requiredMonthly*Math.min(elapsedMonths,goalMonths));


        long differenceAmount =
                (currentAmount == null ? 0L : currentAmount)- expectedAmount;


        PaceStatus status;

        if (differenceAmount > 0) {
            status = PaceStatus.AHEAD;
        } else if (differenceAmount < 0) {
            status = PaceStatus.BEHIND;
        } else {
            status = PaceStatus.ON_TRACK;
        }


        return GoalPaceResponse.builder()
                .expectedAmount(expectedAmount)
                .differenceAmount(differenceAmount)
                .paceStatus(status)
                .build();
    }

    private long calculateRequiredMonthly(
            long goalAmount,
            long startAmount,
            long goalMonths
    ) {
        if (goalMonths <= 0) {
            throw new BusinessException(
                    CommonErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (startAmount >= goalAmount) {
            return 0L;
        }

        return (goalAmount - startAmount + goalMonths - 1) / goalMonths;
    }

    private void validateGoalInput(
            Long goalAmount,
            Long startAmount,
            Integer goalMonths
    ) {

        if (goalAmount == null || goalAmount <= 0) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }

        if (startAmount == null || startAmount < 0) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }

        if (goalMonths == null || goalMonths <= 0) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
    }


}
