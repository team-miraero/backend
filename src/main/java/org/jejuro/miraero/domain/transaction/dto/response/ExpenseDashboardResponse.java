package org.jejuro.miraero.domain.transaction.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExpenseDashboardResponse {
    private Integer year;
    private Integer month;
    private List<RecentTransactionResponse> recentTransactions;
    private CategoryThreeMonthAverageResponse categoryThreeMonthAverages;
    private List<CategoryMonthChangeResponse> categoryMonthChanges;

    public ExpenseDashboardResponse(
            Integer year,
            Integer month,
            List<RecentTransactionResponse> recentTransactions,
            CategoryThreeMonthAverageResponse categoryThreeMonthAverages
    ) {
        this(year, month, recentTransactions, categoryThreeMonthAverages, java.util.Collections.emptyList());
    }
}
