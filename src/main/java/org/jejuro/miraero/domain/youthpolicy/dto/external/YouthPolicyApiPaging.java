package org.jejuro.miraero.domain.youthpolicy.dto.external;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class YouthPolicyApiPaging {

    private Integer totCount;
    private Integer pageNum;
    private Integer pageSize;
}
