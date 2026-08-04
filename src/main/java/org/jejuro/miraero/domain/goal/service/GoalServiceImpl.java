package org.jejuro.miraero.domain.goal.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.domain.GoalStatus;
import org.jejuro.miraero.domain.goal.domain.PaceStatus;
import org.jejuro.miraero.domain.goal.dto.request.GoalAssetRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalCreateRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalPossibilityRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalUpdateRequest;
import org.jejuro.miraero.domain.goal.dto.response.*;
import org.jejuro.miraero.domain.goal.exception.GoalErrorCode;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
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
     * 목표 실현 가능성 조회
     *
     * 목표 금액, 시작 금액, 목표 기간을 기준으로
     * 필요한 월 저축 금액을 계산하고 사용 가능한 금액과 비교하여
     * 목표 달성 가능 여부를 반환한다.
     *
     * @param request 목표 실현 가능성 조회 요청 정보
     * @return 목표 실현 가능성 정보
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
     * 목표 생성
     *
     * 사용자가 입력한 목표 정보를 기반으로 목표를 생성하고,
     * 생성된 목표와 연결할 자산 정보를 저장한다.
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
                userId,
                goal.getGoalId(),
                request.getAssets()
        );


        return GoalCreateResponse.builder()
                .goalId(goal.getGoalId())
                .build();
    }

    /**
     * 사용자의 목표 목록을 조회
     *
     * 사용자가 생성한 목표 목록을 조회하고,
     * 각 목표의 현재 달성 금액 및 진행률을 계산하여 반환한다.
     *
     * @param userId 사용자 ID
     * @return 사용자의 목표 목록
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
     * 목표 진행률 계산 소수점 버림 처리
     */
    private Integer calculateProgressRate(Long currentAmount, Long goalAmount) {
        if (goalAmount == null || goalAmount == 0) {
            return 0;
        }

        if(currentAmount == null) {
            return 0;
        }

        return Math.min(100,
                (int) (currentAmount * 100.0 / goalAmount)
        );
    }

    /**
     * 목표 상세 조회
     *
     * 특정 목표의 기본 정보와 연결된 자산 정보를 기반으로
     * 현재 진행률, 기간 정보, 목표 달성 페이스를 계산하여 반환한다.
     *
     * @param userId 사용자 ID
     * @param goalId 목표 ID
     * @return 목표 상세 정보
     */
    @Override
    @Transactional
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

        // 목표 완료 처리
        if (progressRate == 100
                && goal.getGoalStatus() != GoalStatus.COMPLETED) {

            goalMapper.updateCompleteStatus(goal.getGoalId());
            goal.changeStatus(GoalStatus.COMPLETED);
        }

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

    /**
     * 목표 수정
     *
     * 사용자가 생성한 목표 정보를 수정한다.
     * 요청 값 중 전달된 값만 변경하며,
     * 전달되지 않은 값은 기존 정보를 유지한다.
     *
     * @param userId 사용자 ID
     * @param goalId 목표 ID
     * @param request 목표 수정 요청 정보
     */
    @Override
    @Transactional
    public void updateGoal(Long userId, Long goalId, GoalUpdateRequest request) {

        Goal goal = goalMapper.findByIdAndUserId(userId, goalId);

        if (goal == null) {
            throw new BusinessException(GoalErrorCode.GOAL_NOT_FOUND);
        }

        validateUpdateRequest(request);

        LocalDate goalDate = null;

        if (request.getGoalMonths() != null) {
            goalDate = LocalDate.now()
                    .plusMonths(request.getGoalMonths())
                    .with(TemporalAdjusters.lastDayOfMonth());
        }

        goal.update(
                request.getGoalName(),
                request.getGoalAmount(),
                goalDate
        );

        goalMapper.update(goal);
    }

    private void validateUpdateRequest(GoalUpdateRequest request) {

        if (request.getGoalAmount() != null
                && request.getGoalAmount() <= 0) {
            throw new BusinessException(
                    CommonErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (request.getGoalName() != null
                && request.getGoalName().isBlank()) {
            throw new BusinessException(
                    CommonErrorCode.INVALID_INPUT_VALUE
            );
        }

        Integer goalMonths = request.getGoalMonths();

        if (goalMonths != null && goalMonths <= 0) {
            throw new BusinessException(
                    CommonErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    /**
     * 목표 삭제
     *
     * 사용자가 생성한 목표를 삭제한다.
     * 목표와 연결된 자산 정보는 FK CASCADE 설정에 의해 함께 삭제된다.
     *
     * @param userId 사용자 ID
     * @param goalId 목표 ID
     */
    @Override
    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        Goal goal = goalMapper.findByIdAndUserId(userId,goalId);

        if (goal == null) {
            throw new BusinessException(GoalErrorCode.GOAL_NOT_FOUND);
        }

        // 목표 삭제
        goalMapper.delete(goalId);
    }

    /**
     * 목표 컬렉션 저장
     *
     * 사용자가 완료한 목표를 컬렉션에 저장한다.
     * 본인의 목표인지 검증하며, 목표 상태가 COMPLETED인 경우에만 저장 가능하다.
     *
     * @param userId 사용자ID
     * @param goalId 저장할 목표 ID
     */
    @Override
    @Transactional
    public void saveCollection(Long userId, Long goalId) {

        Goal goal = goalMapper.findByIdAndUserId(userId,goalId);

        if( goal == null){
            throw new BusinessException(GoalErrorCode.GOAL_NOT_FOUND);
        }

        if(goal.getGoalStatus() != GoalStatus.COMPLETED){
            throw new BusinessException(GoalErrorCode.GOAL_NOT_COMPLETED);
        }

        goalMapper.updateCollection(userId,goalId);
    }


    /**
     * 목표 컬렉션 조회
     *
     * 사용자가 컬렉션에 저장한 완료 목표 목록을 조회한다.
     *
     * @param userId 사용자 ID
     * @return 컬렉션 목표 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<GoalCollectionResponse> getGoalCollections(Long userId) {

        List<Goal> goals = goalMapper.findGoalCollectionsByUserId(userId);

        return goals.stream()
                .map(GoalCollectionResponse::from)
                .toList();
    }


}
