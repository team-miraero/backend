package org.jejuro.miraero.domain.product.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SavingOptionResponse {

    private Long savingOptionId;
    private String interestRateType;
    private String reserveType;
    private Integer saveTerm;
    private BigDecimal baseInterestRate;
    private BigDecimal maxInterestRate;
}
