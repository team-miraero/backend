package org.jejuro.miraero.domain.goal.service;

import org.jejuro.miraero.domain.goal.domain.AssetType;
import org.jejuro.miraero.domain.goal.domain.GoalAsset;
import org.jejuro.miraero.domain.goal.dto.request.GoalAssetRequest;
import org.jejuro.miraero.domain.goal.mapper.GoalAssetMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalAssetServiceImplTest {

    @Mock
    private GoalAssetMapper goalAssetMapper;

    @InjectMocks
    private GoalAssetServiceImpl goalAssetService;


    @Test
    @DisplayName("목표 연결 자산 저장 성공")
    void saveGoalAssets_success() {

        // given
        Long goalId = 1L;

        List<GoalAssetRequest> assets = List.of(
                GoalAssetRequest.builder()
                        .assetId(10L)
                        .assetType(AssetType.ACCOUNT)
                        .build()
        );


        // when
        goalAssetService.saveGoalAssets(
                goalId,
                assets
        );


        // then
        verify(goalAssetMapper)
                .saveAll(goalId,anyList());
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


        // when
        Long result =
                goalAssetService.calculateCurrentAmount(goalId);


        // then
        assertEquals(0L, result);
    }


}