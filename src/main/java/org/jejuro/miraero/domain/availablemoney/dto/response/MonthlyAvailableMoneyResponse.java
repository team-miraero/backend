package org.jejuro.miraero.domain.availablemoney.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonthlyAvailableMoneyResponse {
    private Long monthlyIncome;
    private Long fixedExpense;
    private Long variableExpense;
    private Long targetGoalAutoTransfer;
    private Long otherGoalAutoTransfer;
    private Long monthlyAvailableMoney;
}
