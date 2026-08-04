package org.jejuro.miraero.domain.goal.dto.response.asset;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WithdrawalAccountResponse {
    private String bankName;
    private String accountNumberMasked;
}
