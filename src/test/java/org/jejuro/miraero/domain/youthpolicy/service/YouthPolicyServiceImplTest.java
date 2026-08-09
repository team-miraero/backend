package org.jejuro.miraero.domain.youthpolicy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.jejuro.miraero.domain.youthpolicy.domain.YouthPolicyDetailQueryResult;
import org.jejuro.miraero.domain.youthpolicy.domain.YouthPolicyListQueryResult;
import org.jejuro.miraero.domain.youthpolicy.dto.response.YouthPolicyDetailResponse;
import org.jejuro.miraero.domain.youthpolicy.dto.response.YouthPolicyListResponse;
import org.jejuro.miraero.domain.youthpolicy.mapper.YouthPolicyMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.response.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class YouthPolicyServiceImplTest {

    @Mock
    private YouthPolicyMapper youthPolicyMapper;

    private YouthPolicyService youthPolicyService;

    @BeforeEach
    void setUp() {
        youthPolicyService = new YouthPolicyServiceImpl(youthPolicyMapper);
    }

    @Test
    void getYouthPolicies_returnsMappedPageWithoutFilters() {
        when(youthPolicyMapper.findYouthPolicies(null, null, null, 0L, 10))
                .thenReturn(List.of(listResult(
                        1L,
                        LocalDate.of(2026, 7, 28),
                        LocalDate.of(2026, 8, 7),
                        null
                )));
        when(youthPolicyMapper.countYouthPolicies(null, null, null)).thenReturn(1L);

        PageResponse<YouthPolicyListResponse> response = youthPolicyService
                .getYouthPolicies(null, null, null, 1, 10);

        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(1L, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        assertEquals(1L, response.getContent().get(0).getYouthPolicyId());
        assertEquals("청년 전세 지원", response.getContent().get(0).getPolicyName());
        assertEquals("2026-07-28 ~ 2026-08-07", response.getContent().get(0).getApplicationPeriod());
        verify(youthPolicyMapper).findYouthPolicies(null, null, null, 0L, 10);
        verify(youthPolicyMapper).countYouthPolicies(null, null, null);
    }

    @Test
    void getYouthPolicies_passesAllFiltersAndCalculatesOffset() {
        when(youthPolicyMapper.findYouthPolicies("주거지원", "서울", "전세", 10L, 10))
                .thenReturn(Collections.emptyList());
        when(youthPolicyMapper.countYouthPolicies("주거지원", "서울", "전세")).thenReturn(0L);

        PageResponse<YouthPolicyListResponse> response = youthPolicyService
                .getYouthPolicies("주거지원", "서울", "전세", 2, 10);

        assertEquals(1, response.getPage());
        assertEquals(0, response.getContent().size());
        verify(youthPolicyMapper).findYouthPolicies("주거지원", "서울", "전세", 10L, 10);
        verify(youthPolicyMapper).countYouthPolicies("주거지원", "서울", "전세");
    }

    @Test
    void getYouthPolicies_convertsApplicationPeriods() {
        when(youthPolicyMapper.findYouthPolicies(null, null, null, 0L, 10)).thenReturn(List.of(
                listResult(1L, LocalDate.of(2026, 7, 28), LocalDate.of(2026, 8, 7), null),
                listResult(2L, LocalDate.of(2026, 7, 28), null, null),
                listResult(3L, null, LocalDate.of(2026, 8, 7), null),
                listResult(4L, null, null, "상시"),
                listResult(5L, null, null, null)
        ));
        when(youthPolicyMapper.countYouthPolicies(null, null, null)).thenReturn(5L);

        PageResponse<YouthPolicyListResponse> response = youthPolicyService
                .getYouthPolicies(null, null, null, 1, 10);

        assertEquals("2026-07-28 ~ 2026-08-07", response.getContent().get(0).getApplicationPeriod());
        assertEquals("2026-07-28 ~", response.getContent().get(1).getApplicationPeriod());
        assertEquals("~ 2026-08-07", response.getContent().get(2).getApplicationPeriod());
        assertEquals("상시", response.getContent().get(3).getApplicationPeriod());
        assertNull(response.getContent().get(4).getApplicationPeriod());
    }

    @Test
    void getYouthPolicies_returnsEmptyPageWhenNoPoliciesExist() {
        when(youthPolicyMapper.findYouthPolicies(null, null, null, 0L, 10))
                .thenReturn(Collections.emptyList());
        when(youthPolicyMapper.countYouthPolicies(null, null, null)).thenReturn(0L);

        PageResponse<YouthPolicyListResponse> response = youthPolicyService
                .getYouthPolicies(null, null, null, 1, 10);

        assertEquals(0, response.getContent().size());
        assertEquals(0L, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
        assertEquals(true, response.isFirst());
        assertEquals(true, response.isLast());
    }

    @Test
    void getYouthPolicies_rejectsInvalidPageValues() {
        assertInvalidPage(0, 10);
        assertInvalidPage(1, 0);
        assertInvalidPage(1, 101);

        verifyNoInteractions(youthPolicyMapper);
    }

    @Test
    void getYouthPolicyDetail_returnsMappedResponse() {
        when(youthPolicyMapper.findYouthPolicyById(1L)).thenReturn(detailResult(
                LocalDate.of(2026, 7, 28),
                LocalDate.of(2026, 8, 7),
                null
        ));

        YouthPolicyDetailResponse response = youthPolicyService.getYouthPolicyDetail(1L);

        assertEquals(1L, response.getYouthPolicyId());
        assertEquals("청년 전세 지원", response.getPolicyName());
        assertEquals("주거지원", response.getPolicyKeyword());
        assertEquals("정책 설명", response.getPolicyDescription());
        assertEquals("지원 내용", response.getSupportContent());
        assertEquals("서울특별시", response.getProviderInstitutionName());
        assertEquals("2026-07-28 ~ 2026-08-07", response.getApplicationPeriod());
        assertEquals(19, response.getMinAge());
        assertEquals(39, response.getMaxAge());
        assertEquals(60_000_000L, response.getMaxIncome());
        assertEquals("온라인 신청", response.getApplicationMethod());
        assertEquals("https://example.com/apply", response.getApplicationUrl());
        verify(youthPolicyMapper).findYouthPolicyById(1L);
    }

    @Test
    void getYouthPolicyDetail_usesSameApplicationPeriodConversion() {
        when(youthPolicyMapper.findYouthPolicyById(1L)).thenReturn(detailResult(
                null,
                LocalDate.of(2026, 8, 7),
                null
        ));

        YouthPolicyDetailResponse response = youthPolicyService.getYouthPolicyDetail(1L);

        assertEquals("~ 2026-08-07", response.getApplicationPeriod());
    }

    @Test
    void getYouthPolicyDetail_throwsNotFoundWhenPolicyDoesNotExist() {
        when(youthPolicyMapper.findYouthPolicyById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> youthPolicyService.getYouthPolicyDetail(999L)
        );

        assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getYouthPolicyDetail_handlesNullableColumns() {
        when(youthPolicyMapper.findYouthPolicyById(1L)).thenReturn(new YouthPolicyDetailQueryResult(
                1L,
                "청년 정책",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        YouthPolicyDetailResponse response = youthPolicyService.getYouthPolicyDetail(1L);

        assertNull(response.getPolicyKeyword());
        assertNull(response.getProviderInstitutionName());
        assertNull(response.getApplicationPeriod());
        assertNull(response.getMinAge());
        assertNull(response.getApplicationUrl());
    }

    private void assertInvalidPage(int page, int size) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> youthPolicyService.getYouthPolicies(null, null, null, page, size)
        );

        assertEquals(CommonErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
    }

    private YouthPolicyListQueryResult listResult(
            Long youthPolicyId,
            LocalDate applicationStartDate,
            LocalDate applicationEndDate,
            String applicationPeriodText
    ) {
        return new YouthPolicyListQueryResult(
                youthPolicyId,
                "청년 전세 지원",
                "주거지원",
                "서울특별시",
                applicationStartDate,
                applicationEndDate,
                applicationPeriodText
        );
    }

    private YouthPolicyDetailQueryResult detailResult(
            LocalDate applicationStartDate,
            LocalDate applicationEndDate,
            String applicationPeriodText
    ) {
        return new YouthPolicyDetailQueryResult(
                1L,
                "청년 전세 지원",
                "주거지원",
                "정책 설명",
                "지원 내용",
                "서울특별시",
                applicationStartDate,
                applicationEndDate,
                applicationPeriodText,
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
