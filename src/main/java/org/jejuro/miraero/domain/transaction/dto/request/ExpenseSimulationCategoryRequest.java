package org.jejuro.miraero.domain.transaction.dto.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSimulationCategoryRequest {

    @NotNull
    @Positive
    private Long categoryId;

    @NotNull
    @PositiveOrZero
    private Long targetExpense;
}
