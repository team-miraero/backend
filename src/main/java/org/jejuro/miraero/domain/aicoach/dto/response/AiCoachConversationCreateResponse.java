package org.jejuro.miraero.domain.aicoach.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachConversation;

@Getter
@AllArgsConstructor
public class AiCoachConversationCreateResponse {

    private Long aiCoachConversationId;
    private String title;
    private LocalDateTime createdAt;

    public static AiCoachConversationCreateResponse from(AiCoachConversation conversation) {
        return new AiCoachConversationCreateResponse(
                conversation.getAiCoachConversationId(),
                conversation.getTitle(),
                conversation.getCreatedAt()
        );
    }
}
