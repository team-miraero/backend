package org.jejuro.miraero.domain.moneybox.dto.response;

import lombok.Builder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBoxType;

@Getter
@Builder
@ApiModel(description = "머니박스 생성 응답")
public class MoneyBoxCreateResponse {

    @ApiModelProperty(value = "머니박스 ID", example = "1")
    private Long moneyBoxId;
    @ApiModelProperty(value = "머니박스 유형")
    private MoneyBoxType moneyBoxType;
}
