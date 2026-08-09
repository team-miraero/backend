package org.jejuro.miraero.domain.youthpolicy.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.jejuro.miraero.domain.youthpolicy.dto.response.YouthPolicyDetailResponse;
import org.jejuro.miraero.domain.youthpolicy.dto.response.YouthPolicyListResponse;
import org.jejuro.miraero.domain.youthpolicy.service.YouthPolicyService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.exception.GlobalExceptionHandler;
import org.jejuro.miraero.global.response.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class YouthPolicyControllerTest {

    @Mock
    private YouthPolicyService youthPolicyService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new YouthPolicyController(youthPolicyService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getYouthPolicies_passesQueryParametersAndReturnsApiResponse() throws Exception {
        given(youthPolicyService.getYouthPolicies("주거지원", "서울", "전세", 1, 10))
                .willReturn(PageResponse.of(List.of(listResponse()), 0, 10, 1L));

        mockMvc.perform(get("/api/youth-policies")
                        .param("keyword", "주거지원")
                        .param("region", "서울")
                        .param("search", "전세")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].youthPolicyId").value(1))
                .andExpect(jsonPath("$.data.content[0].policyName").value("청년 전세 지원"))
                .andExpect(jsonPath("$.data.content[0].applicationPeriod")
                        .value("2026-07-28 ~ 2026-08-07"));

        verify(youthPolicyService).getYouthPolicies("주거지원", "서울", "전세", 1, 10);
    }

    @Test
    void getYouthPolicies_usesDefaultPaginationWhenQueryParametersAreOmitted() throws Exception {
        given(youthPolicyService.getYouthPolicies(null, null, null, 1, 10))
                .willReturn(PageResponse.of(Collections.emptyList(), 0, 10, 0L));

        mockMvc.perform(get("/api/youth-policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10));

        verify(youthPolicyService).getYouthPolicies(null, null, null, 1, 10);
    }

    @Test
    void getYouthPolicyDetail_passesPathVariableAndReturnsApiResponse() throws Exception {
        given(youthPolicyService.getYouthPolicyDetail(1L)).willReturn(detailResponse());

        mockMvc.perform(get("/api/youth-policies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.youthPolicyId").value(1))
                .andExpect(jsonPath("$.data.policyName").value("청년 전세 지원"))
                .andExpect(jsonPath("$.data.applicationPeriod")
                        .value("2026-07-28 ~ 2026-08-07"));

        verify(youthPolicyService).getYouthPolicyDetail(1L);
    }

    @Test
    void getYouthPolicyDetail_returnsNotFoundError() throws Exception {
        given(youthPolicyService.getYouthPolicyDetail(999L))
                .willThrow(new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND));

        mockMvc.perform(get("/api/youth-policies/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_004"));
    }

    private YouthPolicyListResponse listResponse() {
        return new YouthPolicyListResponse(
                1L,
                "청년 전세 지원",
                "주거지원",
                "서울특별시",
                "2026-07-28 ~ 2026-08-07"
        );
    }

    private YouthPolicyDetailResponse detailResponse() {
        return new YouthPolicyDetailResponse(
                1L,
                "청년 전세 지원",
                "주거지원",
                "정책 설명",
                "지원 내용",
                "서울특별시",
                LocalDate.of(2026, 7, 28),
                LocalDate.of(2026, 8, 7),
                "2026-07-28 ~ 2026-08-07",
                19,
                39,
                null,
                60_000_000L,
                "소득 조건",
                "신청 자격",
                "온라인 신청",
                "https://example.com/apply",
                "https://example.com/reference"
        );
    }
}
