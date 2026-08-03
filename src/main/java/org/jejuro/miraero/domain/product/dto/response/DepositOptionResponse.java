package org.jejuro.miraero.domain.product.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DepositOptionResponse {

    private Long depositOptionId;
    private String interestRateType;
    private Integer saveTerm;
    private BigDecimal baseInterestRate;
    private BigDecimal maxInterestRate;
}
