package org.jejuro.miraero.domain.pacemaker.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaceMakerGoalDepositResponse {

  private Long accountId;
  private Long depositedAmount;
  private Long remainingBalance;
}
