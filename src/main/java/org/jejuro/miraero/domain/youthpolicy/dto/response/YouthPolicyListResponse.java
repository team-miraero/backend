package org.jejuro.miraero.domain.youthpolicy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class YouthPolicyListResponse {

    private Long youthPolicyId;
    private String policyName;
    private String policyKeyword;
    private String providerInstitutionName;
    private String applicationPeriod;
    @Getter(AccessLevel.NONE)
    private Long dDay;

    @JsonProperty("dDay")
    public Long getDDay() {
        return dDay;
    }
}
