package org.jejuro.miraero.domain.moneybox.dto.request;


import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBoxType;

@Getter
@NoArgsConstructor
public class MoneyBoxCreateRequest {
    private MoneyBoxType moneyBoxType;

    //private AutoTransferRequest autoTransfer;
}
