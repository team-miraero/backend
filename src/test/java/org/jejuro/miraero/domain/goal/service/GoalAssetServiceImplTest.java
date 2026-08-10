package org.jejuro.miraero.domain.goal.service;

import org.jejuro.miraero.domain.account.dto.response.AccountResponse;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.domain.goal.domain.AssetType;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.domain.GoalAsset;
import org.jejuro.miraero.domain.goal.dto.request.GoalAssetRequest;
import org.jejuro.miraero.domain.goal.mapper.GoalAssetMapper;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalAssetServiceImplTest {

    @Mock
    private GoalAssetMapper goalAssetMapper;

    @Mock
    private GoalMapper goalMapper;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private GoalAssetServiceImpl goalAssetService;


    @Test
    @DisplayName("목표 연결 자산 저장 성공")
    void saveGoalAssets_success() {

        // given
        Long goalId = 1L;
        Long userId = 1L;

        List<GoalAssetRequest> assets = List.of(
                GoalAssetRequest.builder()
                        .assetId(10L)
                        .assetType(AssetType.ACCOUNT)
                        .build()
        );

        given(goalMapper.findByIdAndUserId(userId, goalId))
                .willReturn(Goal.builder().goalId(goalId).userId(userId).build());
        when(accountMapper.existsByIdAndUserId(10L, userId))
                .thenReturn(true);

        // when
        goalAssetService.saveGoalAssets(
                userId,
                goalId,
                assets
        );


        // then
        verify(goalAssetMapper)
                .saveAll(goalId, assets);
    }

    @Test
    @DisplayName("연결하려는 계좌가 내 소유가 아니면 예외 발생")
    void saveGoalAssets_accountNotOwned_fail() {

        Long goalId = 1L;
        Long userId = 1L;

        List<GoalAssetRequest> assets = List.of(
                GoalAssetRequest.builder()
                        .assetId(10L)
                        .assetType(AssetType.ACCOUNT)
                        .build()
        );

        given(goalMapper.findByIdAndUserId(userId, goalId))
                .willReturn(Goal.builder().goalId(goalId).userId(userId).build());
        when(accountMapper.existsByIdAndUserId(10L, userId))
                .thenReturn(false);

        assertThrows(
                BusinessException.class,
                () -> goalAssetService.saveGoalAssets(userId, goalId, assets)
        );

        verify(goalAssetMapper, never())
                .saveAll(anyLong(), anyList());
    }

    @Test
    @DisplayName("연결된 자산의 현재 금액을 계산한다")
    void calculateCurrentAmount_success() {

        // given
        Long goalId = 1L;

        List<GoalAsset> assets = List.of(
                GoalAsset.builder()
                        .goalId(goalId)
                        .assetType(AssetType.ACCOUNT)
                        .assetId(10L)
                        .build()
        );


        when(goalAssetMapper.findByGoalId(goalId))
                .thenReturn(assets);
        when(accountMapper.findResponseById(10L))
                .thenReturn(AccountResponse.builder().accountId(10L).balance(500_000L).build());


        // when
        Long result =
                goalAssetService.calculateCurrentAmount(goalId);


        // then
        assertEquals(500_000L, result);
    }

    @Test
    @DisplayName("이미 연결된 자산이면 예외 발생")
    void saveGoalAssets_duplicate_fail() {

        // given
        Long goalId = 1L;
        Long userId = 1L;

        List<GoalAssetRequest> assets = List.of(
                GoalAssetRequest.builder()
                        .assetId(10L)
                        .assetType(AssetType.ACCOUNT)
                        .build()
        );

        given(goalMapper.findByIdAndUserId(userId, goalId))
                .willReturn(Goal.builder().goalId(goalId).userId(userId).build());
        when(accountMapper.existsByIdAndUserId(10L, userId))
                .thenReturn(true);
        when(goalAssetMapper.existsByAsset(
                AssetType.ACCOUNT,
                10L
        )).thenReturn(true);


        // when & then
        assertThrows(
                BusinessException.class,
                () -> goalAssetService.saveGoalAssets(userId,goalId, assets)
        );


        verify(goalAssetMapper, never())
                .saveAll(anyLong(), anyList());
    }

    @Test
    @DisplayName("ACCOUNT 자산 조회 시 계좌 상세정보를 채워서 반환한다")
    void getGoalAssets_account_fillsAccountDetail() {

        Long goalId = 1L;
        Long userId = 1L;

        List<GoalAsset> assets = List.of(
                GoalAsset.builder()
                        .goalId(goalId)
                        .assetType(AssetType.ACCOUNT)
                        .assetId(10L)
                        .build()
        );

        given(goalMapper.findByIdAndUserId(userId, goalId))
                .willReturn(Goal.builder().goalId(goalId).userId(userId).build());
        when(goalAssetMapper.findByGoalId(goalId))
                .thenReturn(assets);
        when(accountMapper.findResponseById(10L))
                .thenReturn(AccountResponse.builder()
                        .accountId(10L)
                        .accountName("KB 입출금통장")
                        .institutionName("국민은행")
                        .maskedAccountNumber("123*****90")
                        .balance(500_000L)
                        .build());

        var response = goalAssetService.getGoalAssets(userId, goalId);

        assertEquals(1, response.getAssets().size());
        var asset = response.getAssets().get(0);
        assertEquals("KB 입출금통장", asset.getAssetName());
        assertEquals("국민은행", asset.getBankName());
        assertEquals(500_000L, asset.getBalance());
    }

    @Test
    @DisplayName("내 목표가 아니면 자산 조회 시 예외를 던진다")
    void getGoalAssets_notOwned_fail() {

        Long goalId = 1L;
        Long userId = 1L;

        given(goalMapper.findByIdAndUserId(userId, goalId))
                .willReturn(null);

        assertThrows(
                BusinessException.class,
                () -> goalAssetService.getGoalAssets(userId, goalId)
        );

        verify(goalAssetMapper, never()).findByGoalId(anyLong());
    }

    @Test
    @DisplayName("목표 자산 연결 해제 성공")
    void deleteGoalAsset_success() {

        // given
        Long userId = 1L;
        Long goalId = 1L;
        Long assetId = 10L;

        AssetType assetType = AssetType.ACCOUNT;


        Goal goal = Goal.builder()
                .goalId(goalId)
                .userId(userId)
                .build();


        given(goalMapper.findByIdAndUserId(
                userId,
                goalId
        ))
                .willReturn(goal);


        given(goalAssetMapper.existsByGoalIdAndAsset(
                goalId,
                assetType,
                assetId
        ))
                .willReturn(true);



        // when
        goalAssetService.deleteGoalAsset(
                userId,
                goalId,
                assetType,
                assetId
        );


        // then
        verify(goalAssetMapper)
                .delete(
                        goalId,
                        assetType,
                        assetId
                );
    }




}