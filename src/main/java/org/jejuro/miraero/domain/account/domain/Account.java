package org.jejuro.miraero.domain.account.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Account {

  private Long accountId;
  private Long userId;
  private Long financialInstitutionId;
  private Long exAccountId;
  private String accountType;
  private String accountName;
  private byte[] accountNumber;
  private String accountNumberHash;
  private String maskedAccountNumber;
  private Long balance;
  private String accountStatus;
  private LocalDate openedAt;
  private LocalDate maturityAt;
  private BigDecimal interestRate;
  private Long monthlyPaymentLimit;

  public static Account of(
      Long userId,
      Long financialInstitutionId,
      Long exAccountId,
      String accountType,
      String accountName,
      byte[] accountNumber,
      String accountNumberHash,
      String maskedAccountNumber,
      Long balance,
      String accountStatus,
      LocalDate openedAt,
      LocalDate maturityAt,
      BigDecimal interestRate,
      Long monthlyPaymentLimit
  ) {
    return new Account(
        null,
        userId,
        financialInstitutionId,
        exAccountId,
        accountType,
        accountName,
        accountNumber,
        accountNumberHash,
        maskedAccountNumber,
        balance,
        accountStatus,
        openedAt,
        maturityAt,
        interestRate,
        monthlyPaymentLimit
    );
  }
}
