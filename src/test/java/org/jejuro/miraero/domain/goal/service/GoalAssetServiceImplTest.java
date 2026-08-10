package org.jejuro.miraero.domain.goal.service;

import org.jejuro.miraero.domain.account.dto.response.AccountResponse;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.domain.autotransfer.domain.AutoTransfer;
import org.jejuro.miraero.domain.autotransfer.mapper.AutoTransferMapper;
import org.jejuro.miraero.domain.goal.domain.AssetType;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.domain.GoalAsset;
import org.jejuro.miraero.domain.goal.dto.request.GoalAssetRequest;
import org.jejuro.miraero.domain.goal.dto.response.asset.GoalAssetResponse;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalAssetServiceImplTest {

    private static final Long GOAL_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    @Mock
    private GoalAssetMapper goalAssetMapper;

    @Mock
    private GoalMapper goalMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AutoTransferMapper autoTransferMapper;

    @InjectMocks
    private GoalAssetServiceImpl goalAssetService;


    @Test
    @DisplayName("목표 연결 자산 저장 성공")
    void saveGoalAssets_success() {

        List<GoalAssetRequest> assets = List.of(
                GoalAssetRequest.builder()
                        .assetId(10L)
                        .assetType(AssetType.ACCOUNT)
                        .build()
        );

        given(goalMapper.findByIdAndUserId(USER_ID, GOAL_ID))
                .willReturn(Goal.builder().goalId(GOAL_ID).userId(USER_ID).build());
        when(accountMapper.existsByIdAndUserId(10L, USER_ID))
                .thenReturn(true);

        goalAssetService.saveGoalAssets(
                USER_ID,
                GOAL_ID,
                assets
        );

        verify(goalAssetMapper)
                .saveAll(GOAL_ID, assets);
    }

    @Test
    @DisplayName("연결하려는 계좌가 내 소유가 아니면 예외 발생")
    void saveGoalAssets_accountNotOwned_fail() {

        List<GoalAssetRequest> assets = List.of(
                GoalAssetRequest.builder()
                        .assetId(10L)
                        .assetType(AssetType.ACCOUNT)
                        .build()
        );

        given(goalMapper.findByIdAndUserId(USER_ID, GOAL_ID))
                .willReturn(Goal.builder().goalId(GOAL_ID).userId(USER_ID).build());
        when(accountMapper.existsByIdAndUserId(10L, USER_ID))
                .thenReturn(false);

        assertThrows(
                BusinessException.class,
                () -> goalAssetService.saveGoalAssets(USER_ID, GOAL_ID, assets)
        );

        verify(goalAssetMapper, never())
                .saveAll(anyLong(), anyList());
    }

    @Test
    @DisplayName("연결된 자산의 현재 금액을 계산한다")
    void calculateCurrentAmount_success() {

        List<GoalAsset> assets = List.of(
                GoalAsset.builder()
                        .goalId(GOAL_ID)
                        .assetType(AssetType.ACCOUNT)
                        .assetId(10L)
                        .build()
        );

        when(goalAssetMapper.findByGoalId(GOAL_ID))
                .thenReturn(assets);
        when(accountMapper.findResponseById(10L))
                .thenReturn(AccountResponse.builder().accountId(10L).balance(500_000L).build());

        Long result =
                goalAssetService.calculateCurrentAmount(GOAL_ID);

        assertEquals(500_000L, result);
    }

    @Test
    @DisplayName("이미 연결된 자산이면 예외 발생")
    void saveGoalAssets_duplicate_fail() {

        List<GoalAssetRequest> assets = List.of(
                GoalAssetRequest.builder()
                        .assetId(10L)
                        .assetType(AssetType.ACCOUNT)
                        .build()
        );

        given(goalMapper.findByIdAndUserId(USER_ID, GOAL_ID))
                .willReturn(Goal.builder().goalId(GOAL_ID).userId(USER_ID).build());
        when(accountMapper.existsByIdAndUserId(10L, USER_ID))
                .thenReturn(true);
        when(goalAssetMapper.existsByAsset(
                AssetType.ACCOUNT,
                10L
        )).thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> goalAssetService.saveGoalAssets(USER_ID, GOAL_ID, assets)
        );

        verify(goalAssetMapper, never())
                .saveAll(anyLong(), anyList());
    }

    @Test
    @DisplayName("ACCOUNT 자산 조회 시 계좌 상세정보와 자동이체 정보를 채워서 반환한다")
    void getGoalAssets_account_fillsAccountDetailAndAutoTransfer() {

        List<GoalAsset> assets = List.of(
                GoalAsset.builder()
                        .goalId(GOAL_ID)
                        .assetType(AssetType.ACCOUNT)
                        .assetId(10L)
                        .build()
        );

        given(goalMapper.findById(GOAL_ID))
                .willReturn(Goal.builder().goalId(GOAL_ID).userId(USER_ID).build());
        when(goalAssetMapper.findByGoalId(GOAL_ID))
                .thenReturn(assets);
        when(accountMapper.findResponseById(10L))
                .thenReturn(AccountResponse.builder()
                        .accountId(10L)
                        .accountName("KB 독립적금")
                        .institutionName("국민은행")
                        .maskedAccountNumber("123*****90")
                        .balance(500_000L)
                        .build());
        when(autoTransferMapper.findByAsset(AssetType.ACCOUNT, 10L))
                .thenReturn(AutoTransfer.builder()
                        .withdrawalAccountId(20L)
                        .transferAmount(100_000L)
                        .transferDay(10)
                        .build());
        when(accountMapper.findResponseById(20L))
                .thenReturn(AccountResponse.builder()
                        .accountId(20L)
                        .institutionName("국민은행")
                        .maskedAccountNumber("456*****12")
                        .build());

        var response = goalAssetService.getGoalAssets(USER_ID, GOAL_ID);

        assertEquals(1, response.getAssets().size());
        GoalAssetResponse asset = response.getAssets().get(0);
        assertEquals("KB 독립적금", asset.getAssetName());
        assertEquals("국민은행", asset.getBankName());
        assertEquals(500_000L, asset.getBalance());
        assertEquals(100_000L, asset.getAutoTransfer().getAmount());
        assertEquals(10, asset.getAutoTransfer().getTransferDay());
        assertEquals("456*****12", asset.getAutoTransfer().getWithdrawalAccount().getAccountNumberMasked());
    }

    @Test
    @DisplayName("MONEY_BOX 자산은 자동이체 설정이 없으면 autoTransfer가 null이다")
    void getGoalAssets_moneyBox_noAutoTransfer_isNull() {

        List<GoalAsset> assets = List.of(
                GoalAsset.builder()
                        .goalId(GOAL_ID)
                        .assetType(AssetType.MONEY_BOX)
                        .assetId(30L)
                        .build()
        );

        given(goalMapper.findById(GOAL_ID))
                .willReturn(Goal.builder().goalId(GOAL_ID).userId(USER_ID).build());
        when(goalAssetMapper.findByGoalId(GOAL_ID))
                .thenReturn(assets);
        when(autoTransferMapper.findByAsset(AssetType.MONEY_BOX, 30L))
                .thenReturn(null);

        var response = goalAssetService.getGoalAssets(USER_ID, GOAL_ID);

        assertEquals(1, response.getAssets().size());
        assertNull(response.getAssets().get(0).getAutoTransfer());
    }

    @Test
    @DisplayName("목표 자체가 없으면 404에 해당하는 예외를 던진다")
    void getGoalAssets_notFound_throws() {

        given(goalMapper.findById(GOAL_ID))
                .willReturn(null);

        assertThrows(
                BusinessException.class,
                () -> goalAssetService.getGoalAssets(USER_ID, GOAL_ID)
        );

        verify(goalAssetMapper, never()).findByGoalId(anyLong());
    }

    @Test
    @DisplayName("목표는 존재하지만 다른 사용자 소유면 403에 해당하는 예외를 던진다")
    void getGoalAssets_notOwned_throws() {

        given(goalMapper.findById(GOAL_ID))
                .willReturn(Goal.builder().goalId(GOAL_ID).userId(OTHER_USER_ID).build());

        assertThrows(
                BusinessException.class,
                () -> goalAssetService.getGoalAssets(USER_ID, GOAL_ID)
        );

        verify(goalAssetMapper, never()).findByGoalId(anyLong());
    }

    @Test
    @DisplayName("목표 자산 연결 해제 성공")
    void deleteGoalAsset_success() {

        Long assetId = 10L;
        AssetType assetType = AssetType.ACCOUNT;

        Goal goal = Goal.builder()
                .goalId(GOAL_ID)
                .userId(USER_ID)
                .build();

        given(goalMapper.findByIdAndUserId(
                USER_ID,
                GOAL_ID
        ))
                .willReturn(goal);

        given(goalAssetMapper.existsByGoalIdAndAsset(
                GOAL_ID,
                assetType,
                assetId
        ))
                .willReturn(true);

        goalAssetService.deleteGoalAsset(
                USER_ID,
                GOAL_ID,
                assetType,
                assetId
        );

        verify(goalAssetMapper)
                .delete(
                        GOAL_ID,
                        assetType,
                        assetId
                );
    }
}
