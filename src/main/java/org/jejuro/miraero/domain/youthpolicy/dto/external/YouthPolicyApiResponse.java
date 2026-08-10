package org.jejuro.miraero.domain.youthpolicy.dto.external;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class YouthPolicyApiResponse {

    private Integer resultCode;
    private String resultMessage;
    private YouthPolicyApiResult result;
}
