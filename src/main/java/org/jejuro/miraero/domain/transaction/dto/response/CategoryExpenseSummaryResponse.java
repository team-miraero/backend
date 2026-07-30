package org.jejuro.miraero.domain.transaction.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryExpenseSummaryResponse {
    private Long totalExpense;
    private List<CategoryExpenseItemResponse> categories;
}
