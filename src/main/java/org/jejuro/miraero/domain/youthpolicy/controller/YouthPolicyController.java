package org.jejuro.miraero.domain.youthpolicy.controller;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.youthpolicy.dto.response.YouthPolicyDetailResponse;
import org.jejuro.miraero.domain.youthpolicy.dto.response.YouthPolicyListResponse;
import org.jejuro.miraero.domain.youthpolicy.service.YouthPolicyService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.response.PageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/youth-policies")
@RequiredArgsConstructor
public class YouthPolicyController {

    private final YouthPolicyService youthPolicyService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<YouthPolicyListResponse>>> getYouthPolicies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<YouthPolicyListResponse> response = youthPolicyService.getYouthPolicies(
                keyword,
                region,
                search,
                page,
                size
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{youthPolicyId}")
    public ResponseEntity<ApiResponse<YouthPolicyDetailResponse>> getYouthPolicyDetail(
            @PathVariable Long youthPolicyId
    ) {
        YouthPolicyDetailResponse response = youthPolicyService.getYouthPolicyDetail(youthPolicyId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
