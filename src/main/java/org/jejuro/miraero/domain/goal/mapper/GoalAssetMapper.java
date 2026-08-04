package org.jejuro.miraero.domain.goal.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.goal.domain.AssetType;
import org.jejuro.miraero.domain.goal.domain.GoalAsset;
import org.jejuro.miraero.domain.goal.dto.request.GoalAssetRequest;

import java.util.List;

@Mapper
public interface GoalAssetMapper {
    void saveAll(@Param("goalId") Long goalId,
            @Param("assets") List<GoalAssetRequest> assets);
    List<GoalAsset> findByGoalId(@Param("goalId") Long goalId);
    boolean existsByAsset(
            AssetType assetType,
            Long assetId
    );

}
