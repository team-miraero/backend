package org.jejuro.miraero.domain.goal.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.jejuro.miraero.domain.goal.domain.AssetType;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.domain.GoalAsset;
import org.jejuro.miraero.domain.goal.domain.GoalType;
import org.jejuro.miraero.domain.goal.dto.request.GoalAssetRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalCreateRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalPossibilityRequest;
import org.jejuro.miraero.domain.goal.dto.response.GoalCreateResponse;
import org.jejuro.miraero.domain.goal.dto.response.GoalListResponse;
import org.jejuro.miraero.domain.goal.dto.response.GoalPossibilityResponse;
import org.jejuro.miraero.domain.goal.mapper.GoalAssetMapper;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;


@ExtendWith(MockitoExtension.class)
class GoalServiceImplTest {

    @Mock
    private GoalMapper goalMapper;
    @Mock
    private GoalAssetMapper goalAssetMapper;

    @Mock
    private GoalAssetService goalAssetService;

    @InjectMocks
    private GoalServiceImpl goalService;

    private GoalPossibilityRequest createRequest(
            Long goalAmount,
            Integer goalMonths,
            Long startAmount
    ) {
        return GoalPossibilityRequest.builder()
                .goalAmount(goalAmount)
                .goalMonths(goalMonths)
                .startAmount(startAmount)
                .build();
    }

    @Test
    @DisplayName("목표 달성이 가능하면 possible은 true를 반환한다")
    void checkPossibility_possible() {
        // given
        GoalPossibilityRequest request =
                createRequest(1_000_000L, 10, 0L);

        // when
        GoalPossibilityResponse response =
                goalService.checkPossibility(request);

        // then
        assertTrue(response.isPossible());
        assertEquals(100_000L, response.getRequiredMonthly());
        assertEquals(500_000L, response.getAvailableMonthly());
    }

    @Test
    @DisplayName("목표 달성이 불가능하면 possible은 false를 반환한다")
    void checkPossibility_notPossible() {
        // given
        GoalPossibilityRequest request =
                createRequest(10_000_000L, 10, 0L);

        // when
        GoalPossibilityResponse response =
                goalService.checkPossibility(request);

        // then
        assertFalse(response.isPossible());
        assertEquals(1_000_000L, response.getRequiredMonthly());
        assertEquals(500_000L, response.getAvailableMonthly());
    }

    @Test
    @DisplayName("시작 금액을 고려하여 필요 월별 금액을 계산한다")
    void checkPossibility_withStartAmount() {
        // given
        GoalPossibilityRequest request =
                createRequest(1_000_000L, 10, 200_000L);

        // when
        GoalPossibilityResponse response =
                goalService.checkPossibility(request);

        // then
        assertEquals(80_000L, response.getRequiredMonthly());
        assertTrue(response.isPossible());
    }

    @Test
    @DisplayName("목표 생성 성공 시 생성된 goalId를 반환한다.")
    void createGoal_success() {
        // given
        Long userId = 1L;

        GoalCreateRequest request =
                GoalCreateRequest.builder()
                        .goalType(GoalType.WEDDING)
                        .goalName("결혼 자금")
                        .goalAmount(20_000_000L)
                        .goalMonths(24)
                        .startAmount(1_000_000L)
                        .assets(List.of(
                                GoalAssetRequest.builder()
                                        .assetType(AssetType.ACCOUNT)
                                        .assetId(1L)
                                        .build(),
                                GoalAssetRequest.builder()
                                        .assetType(AssetType.MONEY_BOX)
                                        .assetId(2L)
                                        .build()
                        ))
                        .build();


        // goalMapper.save() 실행 시 goalId 주입
        doAnswer(invocation -> {

            Goal goal = invocation.getArgument(0);
            goal.setGoalId(1L);

            return null;

        }).when(goalMapper).save(any(Goal.class));


        // when
        GoalCreateResponse response =
                goalService.createGoal(userId, request);


        // then
        assertEquals(1L, response.getGoalId());


        verify(goalMapper)
                .save(any(Goal.class));


        verify(goalAssetService)
                .saveGoalAssets(
                        eq(1L),
                        eq(request.getAssets())
                );
    }

    @Test
    @DisplayName("사용자의 목표 목록을 조회하여 반환한다.")
    void getGoalsByUserId_success() {
        // given
        Long userId = 1L;

        Goal goal1 = Goal.builder()
                .goalId(1L)
                .goalName("결혼 자금")
                .goalType(GoalType.WEDDING)
                .goalAmount(2_000_000L)
                .startAmount(500_000L)
                .goalStatus("ACTIVE")
                .build();

        Goal goal2 = Goal.builder()
                .goalId(2L)
                .goalName("독립 자금")
                .goalType(GoalType.INDEPENDENCE)
                .goalAmount(10_000_000L)
                .startAmount(200_000L)
                .goalStatus("ACTIVE")
                .build();

        when(goalMapper.findGoalsByUserId(userId)).thenReturn(List.of(goal1,goal2));


        // 연결 자산에서 계산된 현재 금액 Mock
        when(goalAssetService.calculateCurrentAmount(1L)).thenReturn(500_000L);
        when(goalAssetService.calculateCurrentAmount(2L)).thenReturn(200_000L);

        // when
        List<GoalListResponse> result =
                goalService.getGoalsByUserId(userId);

        // then
        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).getGoalId());
        assertEquals("결혼 자금", result.get(0).getGoalName());
        assertEquals(25, result.get(0).getProgressRate());

        assertEquals(2L, result.get(1).getGoalId());
        assertEquals("독립 자금", result.get(1).getGoalName());
        assertEquals(2, result.get(1).getProgressRate());

        verify(goalMapper).findGoalsByUserId(userId);
        verify(goalAssetService)
                .calculateCurrentAmount(1L);

        verify(goalAssetService)
                .calculateCurrentAmount(2L);
    }

}