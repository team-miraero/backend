package org.jejuro.miraero.domain.aicoach.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachConversation;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachMessage;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachMessageSenderType;
import org.jejuro.miraero.domain.aicoach.dto.request.AiCoachMessageCreateRequest;
import org.jejuro.miraero.domain.aicoach.dto.response.AiCoachMessageResponse;
import org.jejuro.miraero.domain.aicoach.mapper.AiCoachConversationMapper;
import org.jejuro.miraero.domain.aicoach.mapper.AiCoachMessageMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiCoachMessageServiceImpl implements AiCoachMessageService {

    private final AiCoachConversationMapper aiCoachConversationMapper;
    private final AiCoachMessageMapper aiCoachMessageMapper;

    @Override
    public List<AiCoachMessageResponse> getMessages(Long userId, Long conversationId) {
        getOwnedConversation(userId, conversationId);

        return aiCoachMessageMapper.findAllByConversationId(conversationId).stream()
                .map(AiCoachMessageResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public AiCoachMessageResponse saveUserMessage(
            Long userId,
            Long conversationId,
            AiCoachMessageCreateRequest request
    ) {
        getOwnedConversation(userId, conversationId);

        LocalDateTime now = LocalDateTime.now();
        AiCoachMessage message = AiCoachMessage.builder()
                .aiCoachConversationId(conversationId)
                .senderType(AiCoachMessageSenderType.USER)
                .content(request.getContent())
                .createdAt(now)
                .build();

        int savedCount = aiCoachMessageMapper.save(message);
        if (savedCount != 1) {
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

        int updatedCount = aiCoachConversationMapper.updateLastMessageAt(userId, conversationId, now);
        if (updatedCount != 1) {
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

        return AiCoachMessageResponse.from(message);
    }

    private AiCoachConversation getOwnedConversation(Long userId, Long conversationId) {
        AiCoachConversation conversation = aiCoachConversationMapper.findByIdAndUserId(userId, conversationId);

        if (conversation == null) {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
        }
        return conversation;
    }
}
