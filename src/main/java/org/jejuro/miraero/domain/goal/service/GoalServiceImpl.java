package org.jejuro.miraero.domain.goal.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.domain.GoalAsset;
import org.jejuro.miraero.domain.goal.dto.request.GoalCreateRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalPossibilityRequest;
import org.jejuro.miraero.domain.goal.dto.response.GoalCreateResponse;
import org.jejuro.miraero.domain.goal.dto.response.GoalPossibilityResponse;
import org.jejuro.miraero.domain.goal.mapper.GoalAssetMapper;
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
    private final GoalAssetMapper goalAssetMapper;

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


    @Override
    @Transactional
    public GoalCreateResponse createGoal(Long userId, GoalCreateRequest request) {

        // TODO: 연결 자산 검증 추가
        // - assetType에 따라 실제 자산 존재 여부 확인
        // - ACCOUNT -> account 테이블 조회
        // - LOAN -> loan 테이블 조회
        // - MONEY_BOX -> money_box 테이블 조회
        // - 존재하지 안흔 자산이면 BusinessException 발생
        /*
    if (request.getAssets() != null && !request.getAssets().isEmpty()) {

        for (GoalAssetRequest asset : request.getAssets()) {

            boolean exists = switch (asset.getAssetType()) {
                case ACCOUNT ->
                        accountMapper.existsById(asset.getAssetId());

                case LOAN ->
                        loanMapper.existsById(asset.getAssetId());

                case MONEY_BOX ->
                        moneyBoxMapper.existsById(asset.getAssetId());
                default -> false;
            };


            if (!exists) {
                throw new BusinessException(
                        ErrorCode.ASSET_NOT_FOUND
                );
            }
        }
    }
    */

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


        //2. 목표 자산 저장
        // TODO: 자산 검증 완료 후 저장하도록 변경 예정
        if (request.getAssets() != null && !request.getAssets().isEmpty()) {

            List<GoalAsset> goalAssets = request.getAssets()
                    .stream()
                    .map(asset -> GoalAsset.builder()
                            .goalId(goal.getGoalId())
                            .assetType(asset.getAssetType())
                            .assetId(asset.getAssetId())
                            .build())
                    .toList();

            goalAssetMapper.saveAll(goalAssets);
        }

        return GoalCreateResponse.builder()
                .goalId(goal.getGoalId())
                .build();
    }
}
