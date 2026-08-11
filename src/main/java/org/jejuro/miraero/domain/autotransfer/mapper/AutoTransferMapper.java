package org.jejuro.miraero.domain.autotransfer.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.autotransfer.domain.AutoTransfer;
import org.jejuro.miraero.domain.goal.domain.AssetType;

@Mapper
public interface AutoTransferMapper {

    void save(AutoTransfer autoTransfer);

    AutoTransfer findByAsset(@Param("assetType")AssetType assetType,
                             @Param("assetId") Long assetId);

    /**
     * 현재 목표 자동이체 금액
     */
    Long findTargetGoalAutoTransferAmount(@Param("goalId") Long goalId);


    /**
     * 다른 목표 자동이체 금액
     */
    Long findOtherGoalAutoTransferAmount(
            @Param("userId") Long userId,
            @Param("goalId") Long goalId
    );



    void deleteByMoneyBoxId(@Param("moneyBoxId") Long moneyBoxId);
}
