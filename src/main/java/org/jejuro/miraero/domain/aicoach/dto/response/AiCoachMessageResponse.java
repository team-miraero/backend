package org.jejuro.miraero.domain.aicoach.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachMessage;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachMessageSenderType;

@Getter
@AllArgsConstructor
public class AiCoachMessageResponse {

    private Long aiCoachMessageId;
    private AiCoachMessageSenderType senderType;
    private String content;
    private LocalDateTime createdAt;

    public static AiCoachMessageResponse from(AiCoachMessage message) {
        return new AiCoachMessageResponse(
                message.getAiCoachMessageId(),
                message.getSenderType(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
