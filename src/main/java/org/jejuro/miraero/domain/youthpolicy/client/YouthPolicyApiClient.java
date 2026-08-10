package org.jejuro.miraero.domain.youthpolicy.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jejuro.miraero.domain.youthpolicy.dto.external.YouthPolicyApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class YouthPolicyApiClient {

    private static final int SUCCESS_RESULT_CODE = 200;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String policyPath;
    private final String apiKey;
    private final Integer pageSize;

    @Autowired
    public YouthPolicyApiClient(
            @Value("${youth-policy.api.base-url}") String baseUrl,
            @Value("${youth-policy.api.policy-path}") String policyPath,
            @Value("${youth-policy.api.key}") String apiKey,
            @Value("${youth-policy.api.page-size}") Integer pageSize
    ) {
        this(new RestTemplate(), new ObjectMapper(), baseUrl, policyPath, apiKey, pageSize);
    }

    YouthPolicyApiClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            String baseUrl,
            String policyPath,
            String apiKey,
            Integer pageSize
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.baseUrl = baseUrl;
        this.policyPath = policyPath;
        this.apiKey = apiKey;
        this.pageSize = pageSize;
    }

    public YouthPolicyApiResponse getYouthPolicies(int pageNum) {
        String requestUrl = createRequestUrl(pageNum);

        try {
            String responseBody = restTemplate.getForObject(requestUrl, String.class);
            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new RuntimeException("온통청년 API 응답이 없습니다.");
            }

            YouthPolicyApiResponse response = objectMapper.readValue(
                    responseBody,
                    YouthPolicyApiResponse.class
            );
            if (response == null) {
                throw new RuntimeException("온통청년 API 응답을 처리할 수 없습니다.");
            }
            if (!Integer.valueOf(SUCCESS_RESULT_CODE).equals(response.getResultCode())) {
                throw new RuntimeException("온통청년 API 요청이 실패했습니다.");
            }
            return response;
        } catch (RestClientException exception) {
            throw new RuntimeException("온통청년 API 호출에 실패했습니다.", exception);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException("온통청년 API 응답 JSON 파싱에 실패했습니다.", exception);
        }
    }

    private String createRequestUrl(int pageNum) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path(policyPath)
                .queryParam("openApiVlak", apiKey)
                .queryParam("pageIndex", pageNum)
                .queryParam("display", pageSize)
                .encode()
                .toUriString();
    }
}
