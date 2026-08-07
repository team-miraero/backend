package org.jejuro.miraero.domain.aicoach.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.aicoach.client.dto.OpenAiResponsesRequest;
import org.jejuro.miraero.domain.aicoach.client.dto.OpenAiResponsesResponse;
import org.jejuro.miraero.global.config.OpenAiProperties;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OpenAiClientImpl implements OpenAiClient {

    private static final String RESPONSES_PATH = "/responses";
    private static final String OUTPUT_TEXT_TYPE = "output_text";

    private final OpenAiProperties openAiProperties;

    @Override
    public String generateText(String prompt) {
        RestTemplate restTemplate = createRestTemplate();
        String requestUrl = UriComponentsBuilder.fromHttpUrl(openAiProperties.getBaseUrl())
                .path(RESPONSES_PATH)
                .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openAiProperties.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OpenAiResponsesRequest> requestEntity = new HttpEntity<>(
                new OpenAiResponsesRequest(openAiProperties.getModel(), prompt),
                headers
        );

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            return extractGeneratedText(responseEntity.getBody());
        } catch (RestClientException | JsonProcessingException exception) {
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(openAiProperties.getTimeoutMs());
        requestFactory.setReadTimeout(openAiProperties.getTimeoutMs());
        return new RestTemplate(requestFactory);
    }

    private String extractGeneratedText(String responseBody) throws JsonProcessingException {
        if (!StringUtils.hasText(responseBody)) {
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }

        OpenAiResponsesResponse response = new ObjectMapper().readValue(
                responseBody,
                OpenAiResponsesResponse.class
        );
        if (response == null || response.getOutput() == null) {
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }

        String generatedText = response.getOutput().stream()
                .map(OpenAiResponsesResponse.Output::getContent)
                .filter(content -> content != null)
                .flatMap(List::stream)
                .filter(content -> OUTPUT_TEXT_TYPE.equals(content.getType()))
                .map(OpenAiResponsesResponse.OutputContent::getText)
                .filter(StringUtils::hasText)
                .reduce("", String::concat);

        if (!StringUtils.hasText(generatedText)) {
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }
        return generatedText;
    }
}
