package org.jejuro.miraero.domain.product.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DepositProductResponse {

    private Long depositProductId;
    private String financialInstitutionName;
    private String productName;
    private BigDecimal maxInterestRate;
    private List<Integer> saveTerms;
    private Long maxLimit;
    private String joinMethod;
    private Boolean hasJoinRestriction;
    private Boolean hasSpecialCondition;
}
