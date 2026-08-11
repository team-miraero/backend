package org.jejuro.miraero.domain.youthpolicy.service;

import org.jejuro.miraero.domain.youthpolicy.dto.response.YouthPolicyDetailResponse;
import org.jejuro.miraero.domain.youthpolicy.dto.response.YouthPolicyListResponse;
import org.jejuro.miraero.global.response.PageResponse;

public interface YouthPolicyService {

    PageResponse<YouthPolicyListResponse> getYouthPolicies(
            Long userId,
            String keyword,
            String region,
            String search,
            int page,
            int size
    );

    YouthPolicyDetailResponse getYouthPolicyDetail(Long youthPolicyId);
}
