package org.jejuro.miraero.domain.moneybox.domain;


import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneyBox {

  private Long moneyBoxId;
  private Long userId;
  private Long balance;
  private String accountNumberHash;
  private byte[] accountNumber;
  private String maskedAccountNumber;
  private MoneyBoxType moneyBoxType;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
