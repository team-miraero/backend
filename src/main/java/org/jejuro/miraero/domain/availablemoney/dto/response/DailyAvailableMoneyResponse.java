package org.jejuro.miraero.domain.availablemoney.dto.response;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DailyAvailableMoneyResponse {
    private Long todayAvailableMoney;
    private Long todayExpense;
    private Long remainingAvailableMoney;
}
