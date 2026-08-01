package org.jejuro.miraero.domain.goal.dto.request;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jejuro.miraero.domain.goal.domain.GoalType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalCreateRequest {

    @NotBlank
    private GoalType goalType;

    @NotBlank
    private String goalName;

    @NotNull
    @Positive
    private Long goalAmount;

    @NotNull
    @Positive
    private Integer goalMonths;

    @NotNull
    @PositiveOrZero
    private Long startAmount;

    private List<GoalAssetRequest> assets;
}
