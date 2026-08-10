package org.jejuro.miraero.domain.goal.service;

import org.jejuro.miraero.domain.goal.domain.AssetType;
import org.jejuro.miraero.domain.goal.dto.request.GoalAssetRequest;
import org.jejuro.miraero.domain.goal.dto.response.asset.GoalAssetListResponse;

import java.util.List;

public interface GoalAssetService {

    void saveGoalAssets(Long userId, Long goalId, List<GoalAssetRequest> assets);

    /**
     * 목표에 연결된 자산의 현재 금액을 계산한다
     *
     * @param goalId 목표 ID
     * @return 목표에 연결된 자산의 현재 금액 합산
     */
    Long calculateCurrentAmount(Long goalId);

    GoalAssetListResponse getGoalAssets(Long userId, Long goalId);

    /**
     * 목표 자산 연결 해제
     *
     * 목표와 연결된 자산을 제거한다.
     *
     * @param userId 사용자 ID
     * @param goalId 목표 ID
     * @param assetType 자산 타입
     * @param assetId 자산 ID
     */
    void deleteGoalAsset(Long userId, Long goalId, AssetType assetType, Long assetId);
}
