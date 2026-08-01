package org.jejuro.miraero.domain.goal.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jejuro.miraero.domain.goal.domain.AssetType;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class GoalAssetRequest {
    private AssetType assetType;
    private Long assetId;
}
