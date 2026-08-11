package org.jejuro.miraero.domain.moneybox.dto.request;


import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jejuro.miraero.domain.autotransfer.dto.request.AutoTransferCreateRequest;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBoxType;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class MoneyBoxCreateRequest {

    // 저금통을 만들 입출금통장. 자동이체를 걸지 않아도 소속 계좌는 반드시 필요하다.
    @NotNull
    private Long accountId;

    @NotNull
    private MoneyBoxType moneyBoxType;

    @Valid
    private AutoTransferCreateRequest autoTransfer;
}
