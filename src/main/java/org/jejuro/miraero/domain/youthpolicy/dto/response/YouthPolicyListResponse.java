package org.jejuro.miraero.domain.youthpolicy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class YouthPolicyListResponse {

    private Long youthPolicyId;
    private String policyName;
    private String policyKeyword;
    private String providerInstitutionName;
    private String applicationPeriod;
}
