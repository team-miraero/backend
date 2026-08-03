package org.jejuro.miraero.domain.transaction.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExpenseDashboardResponse {
    private Integer year;
    private Integer month;
    private CategoryThreeMonthAverageResponse categoryThreeMonthAverages;
    private PeerAverageResponse peerCategoryAverages;
    private List<CategoryMonthChangeResponse> categoryMonthChanges;
}
