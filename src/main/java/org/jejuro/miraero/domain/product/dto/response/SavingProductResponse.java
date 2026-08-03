package org.jejuro.miraero.domain.product.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SavingProductResponse {

    private Long savingProductId;
    private String financialInstitutionName;
    private String productName;
    private BigDecimal highestInterestRate;
    private List<Integer> saveTerms;
    private List<String> reserveTypes;
    private Long maxLimit;
    private String joinMethod;
    private Boolean hasJoinRestriction;
    private Boolean hasSpecialCondition;
}
