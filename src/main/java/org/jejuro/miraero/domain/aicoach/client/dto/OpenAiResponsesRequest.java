package org.jejuro.miraero.domain.aicoach.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OpenAiResponsesRequest {

    private String model;
    private String input;
}
