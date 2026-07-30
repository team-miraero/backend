package org.jejuro.miraero.domain.goal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.dto.request.GoalCreateRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalPossibilityRequest;
import org.jejuro.miraero.domain.goal.dto.response.GoalCreateResponse;
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


@ExtendWith(MockitoExtension.class)
class GoalServiceImplTest {

    @Mock
    private GoalMapper goalMapper;

    @Mock
    private GoalAssetMapper goalAssetMapper;

    @InjectMocks
    private GoalServiceImpl goalService;

    private GoalPossibilityRequest createRequest(
            long goalAmount,
            int goalMonths,
            long startAmount
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

        GoalCreateRequest request = GoalCreateRequest.builder()
                .goalName("제주도 여행")
                .goalAmount(3_000_000L)
                .startAmount(500_000L)
                .goalMonths(12)
                .build();

        // when
        goalService.createGoal(userId, request);

        // then
        verify(goalMapper)
                .save(any(Goal.class));

        verify(goalAssetMapper, never())
                .saveAll(any());
    }

}