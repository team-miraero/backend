package org.jejuro.miraero.domain.youthpolicy.dto.response;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class YouthPolicyDetailResponse {

    private Long youthPolicyId;
    private String policyName;
    private String policyKeyword;
    private String policyDescription;
    private String supportContent;
    private String providerInstitutionName;
    private LocalDate applicationStartDate;
    private LocalDate applicationEndDate;
    private String applicationPeriod;
    private Integer minAge;
    private Integer maxAge;
    private Long minIncome;
    private Long maxIncome;
    private String incomeConditionText;
    private String qualification;
    private String applicationMethod;
    private String applicationUrl;
    private String referenceUrl;
}
