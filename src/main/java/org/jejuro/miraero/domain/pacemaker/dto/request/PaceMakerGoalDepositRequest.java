package org.jejuro.miraero.domain.pacemaker.dto.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaceMakerGoalDepositRequest {

  @NotNull(message = "MoneyBox id is required.")
  private Long moneyBoxId;

  @NotNull(message = "Account id is required.")
  private Long accountId;

  @NotNull(message = "Deposit amount is required.")
  @Positive(message = "Deposit amount must be greater than 0.")
  private Long amount;
}
