package org.jejuro.miraero.domain.moneybox.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBoxType;

@Getter
@Builder
public class MoneyBoxCreateResponse {

    private Long moneyBoxId;
    private Long accountId;
    private MoneyBoxType moneyBoxType;
}