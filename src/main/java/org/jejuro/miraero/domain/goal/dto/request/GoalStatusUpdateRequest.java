package org.jejuro.miraero.domain.goal.dto.request;


import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jejuro.miraero.domain.goal.domain.GoalStatus;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class GoalStatusUpdateRequest {

    @NotNull
    private GoalStatus status;
}
