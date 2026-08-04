package org.jejuro.miraero.domain.product.dto.response;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SavingProductDetailResponse {

    private Long savingProductId;
    private String financialInstitutionName;
    private String productName;
    private String joinMethod;
    private String joinTarget;
    private String joinRestriction;
    private Boolean hasJoinRestriction;
    private String specialCondition;
    private String maturityInterest;
    private Long maxLimit;
    private String notice;
    private String disclosureMonth;
    private LocalDate disclosureStartDate;
    private LocalDate disclosureEndDate;
    private String productPageUrl;
    private List<SavingOptionResponse> options;
}
