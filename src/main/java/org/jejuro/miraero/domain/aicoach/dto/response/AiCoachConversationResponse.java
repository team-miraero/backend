package org.jejuro.miraero.domain.aicoach.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachConversation;

@Getter
@AllArgsConstructor
public class AiCoachConversationResponse {

    private Long aiCoachConversationId;
    private String title;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;

    public static AiCoachConversationResponse from(AiCoachConversation conversation) {
        return new AiCoachConversationResponse(
                conversation.getAiCoachConversationId(),
                conversation.getTitle(),
                conversation.getLastMessageAt(),
                conversation.getCreatedAt()
        );
    }
}
