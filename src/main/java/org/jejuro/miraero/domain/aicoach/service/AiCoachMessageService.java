package org.jejuro.miraero.domain.aicoach.service;

import java.util.List;
import org.jejuro.miraero.domain.aicoach.dto.response.AiCoachMessageResponse;

public interface AiCoachMessageService {

    List<AiCoachMessageResponse> getMessages(Long userId, Long conversationId);
}
