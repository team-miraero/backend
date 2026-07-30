package org.jejuro.miraero.domain.goal.dto.request;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalCreateRequest {
    private String goalType;
    private String goalName;
    private Long goalAmount;
    private Integer goalMonths;
    private Long startAmount;
    private List<GoalAssetRequest> assets;
}
