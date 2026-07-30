package org.jejuro.miraero.domain.goal.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GoalAsset {
    private Long goalAssetId;
    private Long goalId;
    private String assetType;
    private Long assetId;
    private LocalDateTime createdAt;
}
