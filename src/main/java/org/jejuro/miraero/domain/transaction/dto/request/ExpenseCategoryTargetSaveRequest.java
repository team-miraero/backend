package org.jejuro.miraero.domain.transaction.dto.request;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseCategoryTargetSaveRequest {

    @NotEmpty
    @Valid
    private List<ExpenseCategoryTargetItemRequest> targets;
}
