package org.jejuro.miraero.domain.aicoach.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AiCoachMessageCreateRequest {

    @NotBlank
    private String content;
}
