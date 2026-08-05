package org.jejuro.miraero.domain.autotransfer.dto.request;


import lombok.Getter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
public class AutoTransferCreateRequest {

    @NotNull
    private Long withdrawalAccountId;

    @NotNull
    @Positive
    private Long amount;

    @NotNull
    @Min(1)
    @Max(31)
    private Integer transferDay;
}
