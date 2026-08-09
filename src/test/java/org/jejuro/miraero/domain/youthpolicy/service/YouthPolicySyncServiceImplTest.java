package org.jejuro.miraero.domain.youthpolicy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.jejuro.miraero.domain.youthpolicy.domain.YouthPolicy;
import org.jejuro.miraero.domain.youthpolicy.dto.external.YouthPolicyApiItem;
import org.jejuro.miraero.domain.youthpolicy.mapper.YouthPolicyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class YouthPolicySyncServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private YouthPolicyMapper youthPolicyMapper;

    private YouthPolicySyncService youthPolicySyncService;

    @BeforeEach
    void setUp() {
        youthPolicySyncService = new YouthPolicySyncServiceImpl(youthPolicyMapper);
    }

    @Test
    void syncYouthPolicy_convertsApiItemAndUpsertsYouthPolicy() throws Exception {
        youthPolicySyncService.syncYouthPolicy(readItem("""
                {
                  "plcyNo": "20260806005400213322",
                  "plcyNm": "청년 정책",
                  "plcyKywdNm": "주거지원",
                  "plcyExplnCn": "정책 설명",
                  "plcySprtCn": "지원 내용",
                  "sprvsnInstCd": "6130000",
                  "sprvsnInstCdNm": "서울특별시",
                  "aplyYmd": "20260728 ~ 20260807",
                  "plcyAplyMthdCn": "온라인 신청",
                  "aplyUrlAddr": "https://example.com/apply",
                  "refUrlAddr1": "https://example.com/reference",
                  "sprtTrgtMinAge": "19",
                  "sprtTrgtMaxAge": "39",
                  "earnCndSeCd": "0043001",
                  "earnMinAmt": "0",
                  "earnMaxAmt": "60000000",
                  "earnEtcCn": "소득 조건",
                  "addAplyQlfcCndCn": "추가 자격",
                  "ptcpPrpTrgtCn": "참여 대상",
                  "lastMdfcnDt": "2026-08-06 18:39:36"
                }
                """));

        YouthPolicy youthPolicy = capturedYouthPolicy();

        assertEquals("20260806005400213322", youthPolicy.getPolicyNo());
        assertEquals("청년 정책", youthPolicy.getPolicyName());
        assertEquals(LocalDate.of(2026, 7, 28), youthPolicy.getApplicationStartDate());
        assertEquals(LocalDate.of(2026, 8, 7), youthPolicy.getApplicationEndDate());
        assertEquals("20260728 ~ 20260807", youthPolicy.getApplicationPeriodText());
        assertEquals(19, youthPolicy.getMinAge());
        assertEquals(39, youthPolicy.getMaxAge());
        assertEquals(0L, youthPolicy.getMinIncome());
        assertEquals(60_000_000L, youthPolicy.getMaxIncome());
        assertEquals("추가 자격\n참여 대상", youthPolicy.getQualification());
        assertNotNull(youthPolicy.getSyncedAt());
    }

    @Test
    void syncYouthPolicy_handlesUnparseablePeriodAndInvalidNumbers() throws Exception {
        youthPolicySyncService.syncYouthPolicy(readItem("""
                {
                  "plcyNo": "policy-2",
                  "plcyNm": "상시 정책",
                  "aplyYmd": "상시",
                  "sprtTrgtMinAge": "미정",
                  "sprtTrgtMaxAge": "",
                  "earnMinAmt": "invalid",
                  "earnMaxAmt": "0",
                  "ptcpPrpTrgtCn": "참여 대상"
                }
                """));

        YouthPolicy youthPolicy = capturedYouthPolicy();

        assertNull(youthPolicy.getApplicationStartDate());
        assertNull(youthPolicy.getApplicationEndDate());
        assertEquals("상시", youthPolicy.getApplicationPeriodText());
        assertNull(youthPolicy.getMinAge());
        assertNull(youthPolicy.getMaxAge());
        assertNull(youthPolicy.getMinIncome());
        assertEquals(0L, youthPolicy.getMaxIncome());
        assertEquals("참여 대상", youthPolicy.getQualification());
    }

    @Test
    void syncYouthPolicy_returnsNullQualificationWhenSourceFieldsAreBlank() throws Exception {
        youthPolicySyncService.syncYouthPolicy(readItem("""
                {
                  "plcyNo": "policy-3",
                  "plcyNm": "정책",
                  "aplyYmd": "",
                  "addAplyQlfcCndCn": " ",
                  "ptcpPrpTrgtCn": ""
                }
                """));

        YouthPolicy youthPolicy = capturedYouthPolicy();

        assertNull(youthPolicy.getQualification());
        assertNull(youthPolicy.getApplicationStartDate());
        assertNull(youthPolicy.getApplicationEndDate());
    }

    private YouthPolicyApiItem readItem(String json) throws Exception {
        return objectMapper.readValue(json, YouthPolicyApiItem.class);
    }

    private YouthPolicy capturedYouthPolicy() {
        ArgumentCaptor<YouthPolicy> captor = ArgumentCaptor.forClass(YouthPolicy.class);
        verify(youthPolicyMapper).upsert(captor.capture());
        return captor.getValue();
    }
}
