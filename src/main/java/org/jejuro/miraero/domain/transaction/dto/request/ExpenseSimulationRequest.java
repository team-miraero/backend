package org.jejuro.miraero.domain.transaction.dto.request;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSimulationRequest {

    @NotNull
    @Min(2000)
    private Integer year;

    @NotNull
    @Min(1)
    @Max(12)
    private Integer month;

    @NotEmpty
    @Valid
    private List<ExpenseSimulationCategoryRequest> categories;
}
