package org.jejuro.miraero.domain.aicoach.prompt;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.jejuro.miraero.domain.aicoach.context.AiCoachFinancialContext;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachMessage;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachMessageSenderType;
import org.springframework.stereotype.Component;

@Component
public class AiCoachPromptBuilder {

    private static final int MAX_RECENT_MESSAGE_COUNT = 10;
    private static final String UNKNOWN_INFORMATION = "정보 없음";
    private static final DateTimeFormatter GOAL_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy년 M월 d일");
    private static final String SYSTEM_PROMPT = """
            당신은 미래로의 개인 금융 AI 코치입니다.
            제공된 데이터만을 근거로 답변하고, 없는 정보는 추측하지 않습니다.
            확정적인 수익이나 목표 달성을 보장하지 않습니다.
            주식 종목과 매수·매도 추천은 하지 않습니다.
            답변은 현재 상황, 근거, 실천 방안 2~3개, 목표 영향 순서로 간결하게 작성합니다.
            """;
    private static final String FIRST_QUESTION_RESPONSE_FORMAT = """
            첫 질문이므로 아래 형식을 반드시 지키세요.
            TITLE: 대화방 제목
            ANSWER: 답변
            """;
    private static final String FOLLOW_UP_QUESTION_RESPONSE_FORMAT = """
            후속 질문이므로 답변만 작성하세요.
            """;

    public String buildPrompt(
            List<AiCoachMessage> recentMessages,
            String currentQuestion,
            boolean firstQuestion
    ) {
        return buildPrompt(null, recentMessages, currentQuestion, firstQuestion);
    }

    public String buildPrompt(
            AiCoachFinancialContext financialContext,
            List<AiCoachMessage> recentMessages,
            String currentQuestion,
            boolean firstQuestion
    ) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("[SYSTEM]\n")
                .append(SYSTEM_PROMPT)
                .append("\n[FINANCIAL_CONTEXT]\n");
        appendFinancialContext(prompt, financialContext);

        prompt.append("\n[RECENT_CONVERSATION]\n");

        appendRecentMessages(prompt, recentMessages == null ? Collections.emptyList() : recentMessages);

        prompt.append("\n[RESPONSE_FORMAT]\n")
                .append(firstQuestion
                        ? FIRST_QUESTION_RESPONSE_FORMAT
                        : FOLLOW_UP_QUESTION_RESPONSE_FORMAT)
                .append("\n[CURRENT_USER_QUESTION]\n")
                .append("USER: ")
                .append(currentQuestion);
        return prompt.toString();
    }

    private void appendFinancialContext(
            StringBuilder prompt,
            AiCoachFinancialContext financialContext
    ) {
        if (financialContext == null) {
            prompt.append(UNKNOWN_INFORMATION).append('\n');
            return;
        }

        prompt.append("활성 목표:\n");
        appendActiveGoals(prompt, financialContext.getActiveGoals());
        prompt.append("총자산: ")
                .append(formatAmount(financialContext.getTotalAssets()))
                .append('\n');
        prompt.append("총부채: ")
                .append(formatAmount(financialContext.getTotalDebt()))
                .append('\n');
        prompt.append("월 소득: ")
                .append(formatAmount(financialContext.getMonthlyIncome()))
                .append('\n');
        prompt.append("이번 달 총 지출: ")
                .append(formatAmount(financialContext.getCurrentMonthTotalExpense()))
                .append('\n');
        prompt.append("이번 달 카테고리별 지출:\n");
        appendCategoryExpenses(prompt, financialContext.getCurrentMonthCategoryExpenses());
    }

    private void appendActiveGoals(
            StringBuilder prompt,
            List<AiCoachFinancialContext.ActiveGoal> activeGoals
    ) {
        if (activeGoals == null) {
            prompt.append(UNKNOWN_INFORMATION).append('\n');
            return;
        }
        if (activeGoals.isEmpty()) {
            prompt.append("없음\n");
            return;
        }

        for (AiCoachFinancialContext.ActiveGoal activeGoal : activeGoals) {
            prompt.append("- ")
                    .append(formatText(activeGoal.getGoalName()))
                    .append(" | 목표 금액: ")
                    .append(formatAmount(activeGoal.getGoalAmount()))
                    .append(" | 목표일: ")
                    .append(formatDate(activeGoal.getGoalDate()))
                    .append('\n');
        }
    }

    private void appendCategoryExpenses(
            StringBuilder prompt,
            List<AiCoachFinancialContext.CategoryExpense> categoryExpenses
    ) {
        if (categoryExpenses == null) {
            prompt.append(UNKNOWN_INFORMATION).append('\n');
            return;
        }
        if (categoryExpenses.isEmpty()) {
            prompt.append("없음\n");
            return;
        }

        for (AiCoachFinancialContext.CategoryExpense categoryExpense : categoryExpenses) {
            prompt.append("- ")
                    .append(formatText(categoryExpense.getCategoryName()))
                    .append(": ")
                    .append(formatAmount(categoryExpense.getAmount()))
                    .append('\n');
        }
    }

    private String formatText(String value) {
        return value == null ? UNKNOWN_INFORMATION : value;
    }

    private String formatAmount(Long amount) {
        return amount == null
                ? UNKNOWN_INFORMATION
                : String.format(Locale.KOREA, "%,d원", amount);
    }

    private String formatDate(LocalDate date) {
        return date == null ? UNKNOWN_INFORMATION : GOAL_DATE_FORMATTER.format(date);
    }

    private void appendRecentMessages(StringBuilder prompt, List<AiCoachMessage> recentMessages) {
        int startIndex = Math.max(0, recentMessages.size() - MAX_RECENT_MESSAGE_COUNT);

        for (int index = startIndex; index < recentMessages.size(); index++) {
            AiCoachMessage message = recentMessages.get(index);
            prompt.append(getRole(message.getSenderType()))
                    .append(": ")
                    .append(message.getContent())
                    .append('\n');
        }
    }

    private String getRole(AiCoachMessageSenderType senderType) {
        return senderType == AiCoachMessageSenderType.USER ? "USER" : "ASSISTANT";
    }
}
