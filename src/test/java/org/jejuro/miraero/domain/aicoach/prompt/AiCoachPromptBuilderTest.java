package org.jejuro.miraero.domain.aicoach.prompt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachMessage;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachMessageSenderType;
import org.junit.jupiter.api.Test;

class AiCoachPromptBuilderTest {

    private final AiCoachPromptBuilder promptBuilder = new AiCoachPromptBuilder();

    @Test
    void buildPrompt_includesSystemPromptHistoryAndTitleFormatForFirstQuestion() {
        String prompt = promptBuilder.buildPrompt(
                List.of(
                        createMessage(AiCoachMessageSenderType.USER, "현재 저축액은 100만원입니다."),
                        createMessage(AiCoachMessageSenderType.ASSISTANT, "월 저축 계획을 함께 세워보겠습니다.")
                ),
                "다음 달 저축 목표를 알려주세요.",
                true
        );

        assertTrue(prompt.contains("미래로의 개인 금융 AI 코치"));
        assertTrue(prompt.contains("없는 정보는 추측하지 않습니다"));
        assertTrue(prompt.contains("주식 종목과 매수·매도 추천은 하지 않습니다"));
        assertTrue(prompt.contains("USER: 현재 저축액은 100만원입니다."));
        assertTrue(prompt.contains("ASSISTANT: 월 저축 계획을 함께 세워보겠습니다."));
        assertTrue(prompt.contains("TITLE: 대화방 제목"));
        assertTrue(prompt.contains("ANSWER: 답변"));
        assertTrue(prompt.endsWith("USER: 다음 달 저축 목표를 알려주세요."));
    }

    @Test
    void buildPrompt_includesOnlyLatestTenMessagesAndAnswerFormatForFollowUpQuestion() {
        List<AiCoachMessage> recentMessages = new ArrayList<>();
        for (int index = 0; index < 12; index++) {
            AiCoachMessageSenderType senderType = index % 2 == 0
                    ? AiCoachMessageSenderType.USER
                    : AiCoachMessageSenderType.ASSISTANT;
            recentMessages.add(createMessage(senderType, "message-" + index));
        }

        String prompt = promptBuilder.buildPrompt(
                recentMessages,
                "후속 질문입니다.",
                false
        );

        assertFalse(prompt.contains("USER: message-0\n"));
        assertFalse(prompt.contains("ASSISTANT: message-1\n"));
        assertTrue(prompt.contains("USER: message-2"));
        assertTrue(prompt.contains("ASSISTANT: message-3"));
        assertTrue(prompt.contains("ASSISTANT: message-11"));
        assertTrue(prompt.indexOf("message-2") < prompt.indexOf("message-11"));
        assertTrue(prompt.contains("후속 질문이므로 답변만 작성하세요."));
        assertFalse(prompt.contains("TITLE: 대화방 제목"));
        assertTrue(prompt.endsWith("USER: 후속 질문입니다."));
    }

    private AiCoachMessage createMessage(AiCoachMessageSenderType senderType, String content) {
        return AiCoachMessage.builder()
                .senderType(senderType)
                .content(content)
                .createdAt(LocalDateTime.of(2026, 8, 7, 10, 0))
                .build();
    }
}
