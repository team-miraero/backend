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


}
