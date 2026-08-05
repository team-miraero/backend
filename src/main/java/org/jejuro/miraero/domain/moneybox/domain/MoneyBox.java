package org.jejuro.miraero.domain.moneybox.domain;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MoneyBox {
    private Long moneyBoxId;
    private Long userId;
    private Long balance;
    private String accountNumberHash;
    private byte[] accountNumber;
    private String maskedAccountNumber;
    private MoneyBoxType moneyBoxType;
}
