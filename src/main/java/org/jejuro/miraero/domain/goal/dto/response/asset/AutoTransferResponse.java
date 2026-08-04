package org.jejuro.miraero.domain.goal.dto.response.asset;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AutoTransferResponse {
    private Long amount;
    private Integer transferDay;
    private WithdrawalAccountResponse withdrawalAccount;
}
