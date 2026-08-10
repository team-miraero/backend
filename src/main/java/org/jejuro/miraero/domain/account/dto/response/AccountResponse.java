package org.jejuro.miraero.domain.account.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

  private Long accountId;
  private String accountType;
  private String accountName;
  private String institutionName;
  private String maskedAccountNumber;
  private Long balance;
  private String accountStatus;
  private LocalDate openedAt;
  private LocalDate maturityAt;
  private BigDecimal interestRate;
  private Long monthlyPaymentLimit;
}
