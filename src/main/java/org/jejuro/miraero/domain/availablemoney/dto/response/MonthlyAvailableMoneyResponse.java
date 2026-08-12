package org.jejuro.miraero.domain.availablemoney.dto.response;

import lombok.Builder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
@Builder
@ApiModel(description = "목표 월간 가용 금액 정보")
public class MonthlyAvailableMoneyResponse {
    @ApiModelProperty(value = "월 소득(원)")
    private Long monthlyIncome;
    @ApiModelProperty(value = "월 고정 지출(원)")
    private Long fixedExpense;
    @ApiModelProperty(value = "월 변동 지출(원)")
    private Long variableExpense;
    @ApiModelProperty(value = "현재 목표 자동 이체 금액(원)")
    private Long targetGoalAutoTransfer;
    @ApiModelProperty(value = "다른 목표 자동 이체 금액(원)")
    private Long otherGoalAutoTransfer;
    @ApiModelProperty(value = "월 가용 금액(원)")
    private Long monthlyAvailableMoney;
    private Long elapsedDays;
    private Long remainingDays;
    private Long periodDays;
}
