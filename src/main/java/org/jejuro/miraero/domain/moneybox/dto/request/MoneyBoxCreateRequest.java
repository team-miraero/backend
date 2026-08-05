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

    @NotNull
    private MoneyBoxType moneyBoxType;

    @Valid
    private AutoTransferCreateRequest autoTransfer;
}
