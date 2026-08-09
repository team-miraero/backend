package org.jejuro.miraero.domain.youthpolicy.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class YouthPolicyApiClientTest {

    @Test
    void getYouthPolicies_deserializesResponseAndPassesPageNumber() {
        StubRestTemplate restTemplate = new StubRestTemplate();
        YouthPolicyApiClient client = createClient(restTemplate);
        restTemplate.setResponseBody("""
                {
                  "resultCode": 200,
                  "resultMessage": "성공",
                  "unusedField": "ignored",
                  "result": {
                    "pagging": {
                      "totCount": 1,
                      "pageNum": 2,
                      "pageSize": 10
                    },
                    "youthPolicyList": [
                      {
                        "plcyNo": "20260806005400213322",
                        "plcyNm": "청년 정책",
                        "sprtTrgtMinAge": "19",
                        "earnMaxAmt": "0"
                      }
                    ]
                  }
                }
                """);

        assertEquals(200, client.getYouthPolicies(2).getResultCode());
        assertEquals(1, client.getYouthPolicies(2).getResult().getPagging().getTotCount());
        assertEquals("20260806005400213322", client.getYouthPolicies(2)
                .getResult().getYouthPolicyList().get(0).getPlcyNo());
        assertEquals(
                "https://youth.example.com/opi/youthPlcyList.do"
                        + "?openApiVlak=test-key&pageIndex=2&display=10",
                restTemplate.getRequestUrl()
        );
    }

    @Test
    void getYouthPolicies_throwsExceptionWhenResultCodeIsNotSuccessful() {
        StubRestTemplate restTemplate = new StubRestTemplate();
        YouthPolicyApiClient client = createClient(restTemplate);
        restTemplate.setResponseBody("""
                {
                  "resultCode": 500,
                  "resultMessage": "실패"
                }
                """);

        assertThrows(RuntimeException.class, () -> client.getYouthPolicies(1));
    }

    private YouthPolicyApiClient createClient(RestTemplate restTemplate) {
        return new YouthPolicyApiClient(
                restTemplate,
                new ObjectMapper(),
                "https://youth.example.com",
                "/opi/youthPlcyList.do",
                "test-key",
                10
        );
    }

    private static class StubRestTemplate extends RestTemplate {

        private String responseBody;
        private String requestUrl;

        void setResponseBody(String responseBody) {
            this.responseBody = responseBody;
        }

        String getRequestUrl() {
            return requestUrl;
        }

        @Override
        public <T> T getForObject(String url, Class<T> responseType, Object... uriVariables) {
            requestUrl = url;
            return responseType.cast(responseBody);
        }
    }
}
