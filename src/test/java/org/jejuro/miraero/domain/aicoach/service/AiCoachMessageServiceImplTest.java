package org.jejuro.miraero.domain.aicoach.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachConversation;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachMessage;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachMessageSenderType;
import org.jejuro.miraero.domain.aicoach.dto.request.AiCoachMessageCreateRequest;
import org.jejuro.miraero.domain.aicoach.dto.response.AiCoachMessageResponse;
import org.jejuro.miraero.domain.aicoach.mapper.AiCoachConversationMapper;
import org.jejuro.miraero.domain.aicoach.mapper.AiCoachMessageMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AiCoachMessageServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final Long CONVERSATION_ID = 10L;

    @Mock
    private AiCoachConversationMapper aiCoachConversationMapper;

    @Mock
    private AiCoachMessageMapper aiCoachMessageMapper;

    private AiCoachMessageService aiCoachMessageService;

    @BeforeEach
    void setUp() {
        aiCoachMessageService = new AiCoachMessageServiceImpl(
                aiCoachConversationMapper,
                aiCoachMessageMapper
        );
    }

    @Test
    @DisplayName("본인 소유 대화방의 메시지를 Mapper 반환 순서대로 응답 DTO 목록으로 반환한다")
    void getMessages_returnsResponsesInMapperOrderForOwnedConversation() {
        AiCoachMessage firstMessage = createMessage(
                100L,
                AiCoachMessageSenderType.USER,
                "첫 번째 메시지",
                LocalDateTime.of(2026, 8, 7, 10, 0)
        );
        AiCoachMessage secondMessage = createMessage(
                101L,
                AiCoachMessageSenderType.ASSISTANT,
                "두 번째 메시지",
                LocalDateTime.of(2026, 8, 7, 10, 1)
        );
        when(aiCoachConversationMapper.findByIdAndUserId(USER_ID, CONVERSATION_ID))
                .thenReturn(createConversation());
        when(aiCoachMessageMapper.findAllByConversationId(CONVERSATION_ID))
                .thenReturn(List.of(firstMessage, secondMessage));

        List<AiCoachMessageResponse> responses = aiCoachMessageService.getMessages(
                USER_ID,
                CONVERSATION_ID
        );

        assertEquals(2, responses.size());
        assertEquals(100L, responses.get(0).getAiCoachMessageId());
        assertEquals(AiCoachMessageSenderType.USER, responses.get(0).getSenderType());
        assertEquals("첫 번째 메시지", responses.get(0).getContent());
        assertEquals(firstMessage.getCreatedAt(), responses.get(0).getCreatedAt());
        assertEquals(101L, responses.get(1).getAiCoachMessageId());
        assertEquals(AiCoachMessageSenderType.ASSISTANT, responses.get(1).getSenderType());
        assertEquals("두 번째 메시지", responses.get(1).getContent());
        assertEquals(secondMessage.getCreatedAt(), responses.get(1).getCreatedAt());
        verify(aiCoachConversationMapper).findByIdAndUserId(USER_ID, CONVERSATION_ID);
        verify(aiCoachMessageMapper).findAllByConversationId(CONVERSATION_ID);
    }

    @Test
    @DisplayName("본인 소유 대화방에 메시지가 없으면 빈 목록을 반환한다")
    void getMessages_returnsEmptyListWhenNoMessagesExist() {
        when(aiCoachConversationMapper.findByIdAndUserId(USER_ID, CONVERSATION_ID))
                .thenReturn(createConversation());
        when(aiCoachMessageMapper.findAllByConversationId(CONVERSATION_ID)).thenReturn(List.of());

        List<AiCoachMessageResponse> responses = aiCoachMessageService.getMessages(
                USER_ID,
                CONVERSATION_ID
        );

        assertTrue(responses.isEmpty());
        verify(aiCoachConversationMapper).findByIdAndUserId(USER_ID, CONVERSATION_ID);
        verify(aiCoachMessageMapper).findAllByConversationId(CONVERSATION_ID);
    }

    @Test
    @DisplayName("대화방이 없으면 RESOURCE_NOT_FOUND 예외를 발생시키고 메시지를 조회하지 않는다")
    void getMessages_throwsNotFoundWhenConversationDoesNotExist() {
        when(aiCoachConversationMapper.findByIdAndUserId(USER_ID, CONVERSATION_ID)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> aiCoachMessageService.getMessages(USER_ID, CONVERSATION_ID)
        );

        assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
        verify(aiCoachConversationMapper).findByIdAndUserId(USER_ID, CONVERSATION_ID);
        verify(aiCoachMessageMapper, never()).findAllByConversationId(anyLong());
    }

    @Test
    @DisplayName("다른 사용자 소유 대화방이면 RESOURCE_NOT_FOUND 예외를 발생시키고 메시지를 조회하지 않는다")
    void getMessages_throwsNotFoundWhenConversationIsNotOwnedByUser() {
        when(aiCoachConversationMapper.findByIdAndUserId(USER_ID, CONVERSATION_ID)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> aiCoachMessageService.getMessages(USER_ID, CONVERSATION_ID)
        );

        assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
        verify(aiCoachConversationMapper).findByIdAndUserId(USER_ID, CONVERSATION_ID);
        verify(aiCoachMessageMapper, never()).findAllByConversationId(anyLong());
    }

    @Test
    @DisplayName("본인 소유 대화방에 사용자 메시지를 저장하고 마지막 메시지 시각을 갱신한다")
    void saveUserMessage_savesMessageAndUpdatesLastMessageAt() {
        AiCoachMessageCreateRequest request = createRequest("자산 관리 방법을 알려주세요");
        ArgumentCaptor<AiCoachMessage> messageCaptor = ArgumentCaptor.forClass(AiCoachMessage.class);
        ArgumentCaptor<LocalDateTime> lastMessageAtCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        when(aiCoachConversationMapper.findByIdAndUserId(USER_ID, CONVERSATION_ID))
                .thenReturn(createConversation());
        doAnswer(invocation -> {
            AiCoachMessage message = invocation.getArgument(0);
            ReflectionTestUtils.setField(message, "aiCoachMessageId", 100L);
            return 1;
        }).when(aiCoachMessageMapper).save(any(AiCoachMessage.class));
        when(aiCoachConversationMapper.updateLastMessageAt(
                anyLong(),
                anyLong(),
                any(LocalDateTime.class)
        )).thenReturn(1);

        AiCoachMessageResponse response = aiCoachMessageService.saveUserMessage(
                USER_ID,
                CONVERSATION_ID,
                request
        );

        verify(aiCoachConversationMapper).findByIdAndUserId(USER_ID, CONVERSATION_ID);
        verify(aiCoachMessageMapper).save(messageCaptor.capture());
        verify(aiCoachConversationMapper).updateLastMessageAt(
                eq(USER_ID),
                eq(CONVERSATION_ID),
                lastMessageAtCaptor.capture()
        );
        assertEquals(CONVERSATION_ID, messageCaptor.getValue().getAiCoachConversationId());
        assertEquals(AiCoachMessageSenderType.USER, messageCaptor.getValue().getSenderType());
        assertEquals("자산 관리 방법을 알려주세요", messageCaptor.getValue().getContent());
        assertEquals(messageCaptor.getValue().getCreatedAt(), lastMessageAtCaptor.getValue());
        assertEquals(100L, response.getAiCoachMessageId());
        assertEquals(AiCoachMessageSenderType.USER, response.getSenderType());
        assertEquals("자산 관리 방법을 알려주세요", response.getContent());
        assertEquals(messageCaptor.getValue().getCreatedAt(), response.getCreatedAt());
    }

    @Test
    @DisplayName("대화방이 없거나 다른 사용자 소유이면 사용자 메시지를 저장하지 않는다")
    void saveUserMessage_throwsNotFoundWhenConversationIsNotOwnedByUser() {
        when(aiCoachConversationMapper.findByIdAndUserId(USER_ID, CONVERSATION_ID)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> aiCoachMessageService.saveUserMessage(
                        USER_ID,
                        CONVERSATION_ID,
                        createRequest("메시지")
                )
        );

        assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
        verify(aiCoachConversationMapper).findByIdAndUserId(USER_ID, CONVERSATION_ID);
        verify(aiCoachMessageMapper, never()).save(any(AiCoachMessage.class));
        verify(aiCoachConversationMapper, never()).updateLastMessageAt(
                anyLong(),
                anyLong(),
                any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("메시지 저장에 실패하면 마지막 메시지 시각을 갱신하지 않는다")
    void saveUserMessage_throwsInternalServerErrorWhenMessageSaveFails() {
        when(aiCoachConversationMapper.findByIdAndUserId(USER_ID, CONVERSATION_ID))
                .thenReturn(createConversation());
        when(aiCoachMessageMapper.save(any(AiCoachMessage.class))).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> aiCoachMessageService.saveUserMessage(
                        USER_ID,
                        CONVERSATION_ID,
                        createRequest("메시지")
                )
        );

        assertEquals(CommonErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
        verify(aiCoachMessageMapper).save(any(AiCoachMessage.class));
        verify(aiCoachConversationMapper, never()).updateLastMessageAt(
                anyLong(),
                anyLong(),
                any(LocalDateTime.class)
        );
    }

    private AiCoachConversation createConversation() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 7, 10, 0);
        return AiCoachConversation.builder()
                .aiCoachConversationId(CONVERSATION_ID)
                .userId(USER_ID)
                .title("AI 코치 상담")
                .lastMessageAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private AiCoachMessage createMessage(
            Long messageId,
            AiCoachMessageSenderType senderType,
            String content,
            LocalDateTime createdAt
    ) {
        return AiCoachMessage.builder()
                .aiCoachMessageId(messageId)
                .aiCoachConversationId(CONVERSATION_ID)
                .senderType(senderType)
                .content(content)
                .createdAt(createdAt)
                .build();
    }

    private AiCoachMessageCreateRequest createRequest(String content) {
        AiCoachMessageCreateRequest request = new AiCoachMessageCreateRequest();
        ReflectionTestUtils.setField(request, "content", content);
        return request;
    }
}
