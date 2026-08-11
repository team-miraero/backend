package org.jejuro.miraero.domain.aicoach.prompt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.jejuro.miraero.domain.aicoach.context.AiCoachFinancialContext;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachMessage;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachMessageSenderType;
import org.junit.jupiter.api.Test;

class AiCoachPromptBuilderTest {

    private final AiCoachPromptBuilder promptBuilder = new AiCoachPromptBuilder();

    @Test
    void buildPrompt_includesPreviousSummaryAndSummaryResponseFormat() {
        String prompt = promptBuilder.buildPrompt(
                AiCoachFinancialContext.builder().build(),
                "User plans to save 300,000 won monthly.",
                "How should I reduce food spending?",
                false
        );

        assertTrue(prompt.contains("[PREVIOUS_CONVERSATION_SUMMARY]"));
        assertTrue(prompt.contains("User plans to save 300,000 won monthly."));
        assertTrue(prompt.contains("[CURRENT_USER_QUESTION]"));
        assertTrue(prompt.contains("USER: How should I reduce food spending?"));
        assertTrue(prompt.contains("ANSWER: answer"));
        assertTrue(prompt.contains("SUMMARY: updated cumulative conversation summary"));
        assertFalse(prompt.contains("[RECENT_CONVERSATION]"));
    }

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

    @Test
    void buildPrompt_includesFinancialContextWithReadableAmounts() {
        AiCoachFinancialContext financialContext = AiCoachFinancialContext.builder()
                .activeGoals(List.of(new AiCoachFinancialContext.ActiveGoal(
                        "내 집 마련",
                        50_000_000L,
                        LocalDate.of(2027, 12, 31)
                )))
                .totalAssets(12_345_678L)
                .totalDebt(3_000_000L)
                .monthlyIncome(3_200_000L)
                .currentMonthTotalExpense(1_250_000L)
                .currentMonthCategoryExpenses(List.of(
                        new AiCoachFinancialContext.CategoryExpense("식비", 450_000L),
                        new AiCoachFinancialContext.CategoryExpense("교통", 120_000L)
                ))
                .build();

        String prompt = promptBuilder.buildPrompt(
                financialContext,
                List.of(),
                "현재 상황을 분석해주세요.",
                false
        );

        assertTrue(prompt.contains("[FINANCIAL_CONTEXT]"));
        assertTrue(prompt.contains("- 내 집 마련 | 목표 금액: 50,000,000원 | 목표일: 2027년 12월 31일"));
        assertTrue(prompt.contains("총자산: 12,345,678원"));
        assertTrue(prompt.contains("총부채: 3,000,000원"));
        assertTrue(prompt.contains("월 소득: 3,200,000원"));
        assertTrue(prompt.contains("이번 달 총 지출: 1,250,000원"));
        assertTrue(prompt.contains("- 식비: 450,000원"));
        assertTrue(prompt.contains("- 교통: 120,000원"));
    }

    @Test
    void buildPrompt_representsNullFinancialContextValuesAsUnknownInformation() {
        AiCoachFinancialContext financialContext = AiCoachFinancialContext.builder()
                .activeGoals(null)
                .totalAssets(null)
                .totalDebt(null)
                .monthlyIncome(null)
                .currentMonthTotalExpense(null)
                .currentMonthCategoryExpenses(null)
                .build();

        String prompt = promptBuilder.buildPrompt(financialContext, List.of(), "질문", false);

        assertTrue(prompt.contains("활성 목표:\n정보 없음"));
        assertTrue(prompt.contains("총자산: 정보 없음"));
        assertTrue(prompt.contains("총부채: 정보 없음"));
        assertTrue(prompt.contains("월 소득: 정보 없음"));
        assertTrue(prompt.contains("이번 달 총 지출: 정보 없음"));
        assertTrue(prompt.contains("이번 달 카테고리별 지출:\n정보 없음"));
    }

    private AiCoachMessage createMessage(AiCoachMessageSenderType senderType, String content) {
        return AiCoachMessage.builder()
                .senderType(senderType)
                .content(content)
                .createdAt(LocalDateTime.of(2026, 8, 7, 10, 0))
                .build();
    }
}
