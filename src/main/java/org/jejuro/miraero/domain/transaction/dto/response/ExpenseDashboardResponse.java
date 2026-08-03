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
    private PeerAverageResponse peerCategoryAverages;
    private List<CategoryMonthChangeResponse> categoryMonthChanges;

    public ExpenseDashboardResponse(
            Integer year,
            Integer month,
            List<RecentTransactionResponse> recentTransactions,
            CategoryThreeMonthAverageResponse categoryThreeMonthAverages
    ) {
        this(
                year,
                month,
                recentTransactions,
                categoryThreeMonthAverages,
                new PeerAverageResponse(java.util.Collections.emptyList()),
                java.util.Collections.emptyList()
        );
    }

    public ExpenseDashboardResponse(
            Integer year,
            Integer month,
            List<RecentTransactionResponse> recentTransactions,
            CategoryThreeMonthAverageResponse categoryThreeMonthAverages,
            List<CategoryMonthChangeResponse> categoryMonthChanges
    ) {
        this(
                year,
                month,
                recentTransactions,
                categoryThreeMonthAverages,
                new PeerAverageResponse(java.util.Collections.emptyList()),
                categoryMonthChanges
        );
    }
}
