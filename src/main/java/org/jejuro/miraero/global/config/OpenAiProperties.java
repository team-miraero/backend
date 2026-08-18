package org.jejuro.miraero.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class OpenAiProperties {

    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final int timeoutMs;
    private final boolean moderationEnabled;

    public OpenAiProperties(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model}") String model,
            @Value("${openai.base-url}") String baseUrl,
            @Value("${openai.timeout-ms}") int timeoutMs,
            @Value("${openai.moderation.enabled:false}") boolean moderationEnabled
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
        this.timeoutMs = timeoutMs;
        this.moderationEnabled = moderationEnabled;
    }
}
