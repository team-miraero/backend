package org.jejuro.miraero.domain.goal.dto.request;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GoalAssetRequest {
    private String assetType;
    private Long assetId;
}
