package org.jejuro.miraero.domain.goal.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.goal.domain.GoalAsset;

import java.util.List;

@Mapper
public interface GoalAssetMapper {
    void saveAll(@Param("goalAssets") List<GoalAsset> goalAssets);
}
