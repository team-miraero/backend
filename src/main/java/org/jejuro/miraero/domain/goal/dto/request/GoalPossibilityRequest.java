package org.jejuro.miraero.domain.goal.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalPossibilityRequest {

    @NotNull
    @Positive
    private Long goalAmount;

    @NotNull
    @Positive
    private Integer goalMonths;

    @NotNull
    @PositiveOrZero
    private Long startAmount;
}
