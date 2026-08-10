package org.jejuro.miraero.domain.goal.milestone.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.aicoach.client.OpenAiClient;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.jejuro.miraero.domain.goal.milestone.domain.Milestone;
import org.jejuro.miraero.domain.goal.milestone.mapper.MilestoneMapper;
import org.jejuro.miraero.domain.goal.milestone.mapper.MilestoneReportMapper;
import org.jejuro.miraero.domain.goal.milestone.dto.request.MilestoneReportAiRequest;
import org.jejuro.miraero.domain.transaction.domain.ExpenseCategory;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseCategorySummaryResponse;
import org.jejuro.miraero.domain.transaction.mapper.TransactionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MilestoneReportAsyncService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    MilestoneReportAsyncService.class
            );

    /*
     * 마일스톤 단계는 현재 기획상 고정
     */
    private static final List<Integer> MILESTONE_PERCENTAGES =
            List.of(25, 50, 75, 100);

    private final GoalMapper goalMapper;
    private final MilestoneMapper milestoneMapper;
    private final MilestoneReportMapper milestoneReportMapper;
    private final TransactionMapper transactionMapper;
    private final OpenAiClient openAiClient;

    private final ObjectMapper objectMapper =
            new ObjectMapper().findAndRegisterModules();

    @Async("milestoneReportExecutor")
    public void generate(
            Long milestoneId,
            Long goalId,
            Long reportId
    ) {

        try {

            Goal goal =
                    goalMapper.findById(goalId);

            Milestone milestone =
                    milestoneMapper.findById(milestoneId);

            if (goal == null || milestone == null) {
                throw new IllegalArgumentException(
                        "목표 또는 마일스톤을 찾을 수 없습니다."
                );
            }

            MilestoneReportAiRequest request =
                    buildAiRequest(
                            goal,
                            milestone
                    );

            String prompt =
                    buildPrompt(request);

            String response =
                    openAiClient.generateText(prompt);

            ParsedReport parsedReport =
                    parseAiResponse(response);

            int updated =
                    milestoneReportMapper.updateSuccess(
                            reportId,
                            parsedReport.title(),
                            parsedReport.content()
                    );

            if (updated == 0) {
                log.warn(
                        "마일스톤 리포트 성공 상태 업데이트 실패. reportId={}",
                        reportId
                );
                return;
            }

        } catch (Exception e) {

            log.error(
                    "마일스톤 AI 리포트 생성 실패. " +
                            "milestoneId={}, reportId={}",
                    milestoneId,
                    reportId,
                    e
            );

            int updated =
                    milestoneReportMapper.updateFailed(reportId);

            if (updated == 0) {
                log.warn(
                        "마일스톤 리포트 실패 상태 업데이트 대상이 없습니다. " +
                                "reportId={}",
                        reportId
                );
            }
        }
    }

    /**
     * AI 요청 데이터 구성
     */
    private MilestoneReportAiRequest buildAiRequest(
            Goal goal,
            Milestone milestone
    ) {

        LocalDate expectedTargetDate =
                calculateTargetDate(
                        goal,
                        milestone.getMilestonePercentage()
                );

        LocalDate reportEndDate =
                resolveReportEndDate(
                        goal,
                        milestone
                );

        Milestone previousMilestone =
                findPreviousDistinctMilestone(
                        goal,
                        milestone
                );

        LocalDate periodStartDate =
                resolveCurrentPeriodStart(
                        goal,
                        previousMilestone
                );

        MilestoneReportAiRequest.ExpenseSummary expenseSummary =
                summarizeExpenses(
                        goal.getUserId(),
                        periodStartDate,
                        reportEndDate,
                        previousMilestone,
                        goal
                );

        return MilestoneReportAiRequest.builder()
                .goal(
                        MilestoneReportAiRequest.GoalInfo.builder()
                                .goalName(
                                        goal.getGoalName()
                                )
                                .goalAmount(
                                        goal.getGoalAmount()
                                )
                                .startAmount(
                                        goal.getStartAmount()
                                )
                                .startDate(
                                        goal.getStartDate()
                                )
                                .goalDate(
                                        goal.getGoalDate()
                                )
                                .build()
                )
                .milestone(
                        MilestoneReportAiRequest.MilestoneInfo.builder()
                                .percentage(
                                        milestone
                                                .getMilestonePercentage()
                                )
                                .milestoneAmount(
                                        milestone
                                                .getMilestoneAmount()
                                )
                                .targetDate(
                                        expectedTargetDate
                                )
                                .achievedAt(
                                        milestone.getAchievedAt() == null
                                                ? null
                                                : milestone
                                                .getAchievedAt()
                                                .toLocalDate()
                                )
                                .build()
                )
                .expenseSummary(
                        expenseSummary
                )
                .build();
    }

    /**
     * 현재 마일스톤 이전의
     * 가장 최근 다른 날짜의 마일스톤
     */
    private Milestone findPreviousDistinctMilestone(
            Goal goal,
            Milestone currentMilestone
    ) {

        if (goal == null
                || goal.getGoalId() == null
                || currentMilestone == null
                || currentMilestone
                .getMilestonePercentage() == null
                || currentMilestone.getAchievedAt() == null) {

            return null;
        }

        LocalDate currentAchievedDate =
                currentMilestone
                        .getAchievedAt()
                        .toLocalDate();

        Integer currentPercentage =
                currentMilestone
                        .getMilestonePercentage();

        Milestone previousMilestone = null;

        for (Integer percentage :
                MILESTONE_PERCENTAGES) {

            if (percentage >= currentPercentage) {
                continue;
            }

            Milestone candidate =
                    milestoneMapper
                            .findByGoalIdAndPercentage(
                                    goal.getGoalId(),
                                    percentage
                            );

            if (candidate == null
                    || !candidate.isAchieved()
                    || candidate.getAchievedAt() == null) {
                continue;
            }

            LocalDate candidateDate =
                    candidate
                            .getAchievedAt()
                            .toLocalDate();

            /*
             * 같은 날짜면 새로운 구간 경계가 아니다.
             */
            if (!candidateDate.isBefore(
                    currentAchievedDate
            )) {
                continue;
            }

            if (previousMilestone == null
                    || candidateDate.isAfter(
                    previousMilestone
                            .getAchievedAt()
                            .toLocalDate()
            )) {

                previousMilestone = candidate;
            }
        }

        return previousMilestone;
    }

    /**
     * 현재 지출 분석 구간 시작일
     */
    private LocalDate resolveCurrentPeriodStart(
            Goal goal,
            Milestone previousMilestone
    ) {

        if (goal == null
                || goal.getStartDate() == null) {
            return null;
        }

        if (previousMilestone == null
                || previousMilestone.getAchievedAt() == null) {

            return goal.getStartDate();
        }

        return previousMilestone
                .getAchievedAt()
                .toLocalDate()
                .plusDays(1);
    }

    /**
     * 현재 기간 + 이전 기간 지출 분석
     */
    private MilestoneReportAiRequest.ExpenseSummary summarizeExpenses(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            Milestone previousMilestone,
            Goal goal
    ) {

        if (startDate == null || endDate == null) {
            return buildEmptyExpenseSummary();
        }

        if (endDate.isBefore(startDate)) {
            return buildEmptyExpenseSummary(
                    startDate,
                    endDate
            );
        }

        List<ExpenseCategorySummaryResponse>
                currentSummaries =
                transactionMapper.findVariableExpenseSummary(
                        userId,
                        startDate,
                        endDate
                );

        PreviousExpensePeriod previousPeriod =
                findPreviousExpensePeriod(
                        userId,
                        previousMilestone,
                        goal
                );

        Map<ExpenseCategory, Long> currentAmounts =
                aggregateByCategory(
                        currentSummaries
                );

        Map<ExpenseCategory, Long> previousAmounts =
                aggregateByCategory(
                        previousPeriod.summaries()
                );

        long totalExpense =
                currentAmounts.values()
                        .stream()
                        .mapToLong(Long::longValue)
                        .sum();

        long currentDays =
                calculateInclusiveDays(
                        startDate,
                        endDate
                );

        long dailyAverageExpense =
                calculateDailyAverage(
                        totalExpense,
                        currentDays
                );

        List<MilestoneReportAiRequest.CategoryExpense>
                categories =
                buildCategories(
                        currentAmounts,
                        previousAmounts,
                        totalExpense,
                        currentDays,
                        previousPeriod.days()
                );

        return MilestoneReportAiRequest.ExpenseSummary.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalExpense(
                        Math.max(0L, totalExpense)
                )
                .dailyAverageExpense(
                        dailyAverageExpense
                )
                .categories(categories)
                .build();
    }

    /**
     * 이전 비교 기간
     */
    private PreviousExpensePeriod findPreviousExpensePeriod(
            Long userId,
            Milestone previousMilestone,
            Goal goal
    ) {

        if (previousMilestone == null
                || previousMilestone.getAchievedAt() == null
                || goal == null
                || goal.getStartDate() == null) {

            return PreviousExpensePeriod.empty();
        }

        LocalDate previousEndDate =
                previousMilestone
                        .getAchievedAt()
                        .toLocalDate();

        Milestone previousPreviousMilestone =
                findPreviousDistinctMilestone(
                        goal,
                        previousMilestone
                );

        LocalDate previousStartDate =
                goal.getStartDate();

        if (previousPreviousMilestone != null
                && previousPreviousMilestone
                .getAchievedAt() != null) {

            previousStartDate =
                    previousPreviousMilestone
                            .getAchievedAt()
                            .toLocalDate()
                            .plusDays(1);
        }

        if (previousEndDate.isBefore(
                previousStartDate
        )) {

            return PreviousExpensePeriod.empty();
        }

        List<ExpenseCategorySummaryResponse>
                summaries =
                transactionMapper.findVariableExpenseSummary(
                        userId,
                        previousStartDate,
                        previousEndDate
                );

        long days =
                calculateInclusiveDays(
                        previousStartDate,
                        previousEndDate
                );

        return new PreviousExpensePeriod(
                previousStartDate,
                previousEndDate,
                days,
                summaries
        );
    }

    private long calculateInclusiveDays(
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (startDate == null
                || endDate == null
                || endDate.isBefore(startDate)) {

            return 0L;
        }

        return ChronoUnit.DAYS.between(
                startDate,
                endDate
        ) + 1L;
    }

    private long calculateDailyAverage(
            long totalExpense,
            long days
    ) {

        if (totalExpense <= 0L
                || days <= 0L) {

            return 0L;
        }

        return Math.round(
                totalExpense / (double) days
        );
    }

    private Map<ExpenseCategory, Long> aggregateByCategory(
            List<ExpenseCategorySummaryResponse> summaries
    ) {

        Map<ExpenseCategory, Long> amounts =
                new EnumMap<>(
                        ExpenseCategory.class
                );

        for (ExpenseCategory category :
                ExpenseCategory.values()) {

            amounts.put(
                    category,
                    0L
            );
        }

        if (summaries == null
                || summaries.isEmpty()) {

            return amounts;
        }

        for (ExpenseCategorySummaryResponse summary :
                summaries) {

            if (summary == null
                    || !StringUtils.hasText(
                    summary.getCategoryName()
            )) {
                continue;
            }

            ExpenseCategory category =
                    findCategory(
                            summary.getCategoryName()
                    );

            long amount =
                    summary.getAmount() == null
                            ? 0L
                            : Math.max(
                            0L,
                            summary.getAmount()
                    );

            amounts.merge(
                    category,
                    amount,
                    Long::sum
            );
        }

        return amounts;
    }

    private ExpenseCategory findCategory(
            String categoryName
    ) {

        String normalizedName =
                categoryName.trim();

        for (ExpenseCategory category :
                ExpenseCategory.values()) {

            if (category.getDisplayName()
                    .equals(normalizedName)) {

                return category;
            }
        }

        return ExpenseCategory.ETC;
    }

    private List<MilestoneReportAiRequest.CategoryExpense>
    buildCategories(
            Map<ExpenseCategory, Long> currentAmounts,
            Map<ExpenseCategory, Long> previousAmounts,
            long totalExpense,
            long currentDays,
            long previousDays
    ) {

        List<MilestoneReportAiRequest.CategoryExpense>
                categories =
                new ArrayList<>();

        for (ExpenseCategory category :
                ExpenseCategory.values()) {

            long currentAmount =
                    currentAmounts.getOrDefault(
                            category,
                            0L
                    );

            long previousAmount =
                    previousAmounts.getOrDefault(
                            category,
                            0L
                    );

            double proportion =
                    calculateProportion(
                            currentAmount,
                            totalExpense
                    );

            double currentDailyAverage =
                    calculateDailyAverageDouble(
                            currentAmount,
                            currentDays
                    );

            double previousDailyAverage =
                    calculateDailyAverageDouble(
                            previousAmount,
                            previousDays
                    );

            Double changeRate =
                    calculateChangeRate(
                            currentDailyAverage,
                            previousDailyAverage
                    );

            categories.add(
                    MilestoneReportAiRequest
                            .CategoryExpense
                            .builder()
                            .category(
                                    category.getDisplayName()
                            )
                            .amount(currentAmount)
                            .proportion(proportion)
                            .changeRate(changeRate)
                            .build()
            );
        }

        categories.sort(
                Comparator.comparingLong(
                        MilestoneReportAiRequest
                                .CategoryExpense
                                ::getAmount
                ).reversed()
        );

        return categories;
    }

    private double calculateProportion(
            long amount,
            long totalExpense
    ) {

        if (amount <= 0L
                || totalExpense <= 0L) {

            return 0.0;
        }

        return Math.round(
                amount * 10000.0 / totalExpense
        ) / 100.0;
    }

    private double calculateDailyAverageDouble(
            long amount,
            long days
    ) {

        if (amount <= 0L
                || days <= 0L) {

            return 0.0;
        }

        return amount / (double) days;
    }

    private Double calculateChangeRate(
            double currentDailyAverage,
            double previousDailyAverage
    ) {

        if (previousDailyAverage <= 0.0) {
            return null;
        }

        return Math.round(
                (
                        (currentDailyAverage
                                - previousDailyAverage)
                                * 10000.0
                                / previousDailyAverage
                )
        ) / 100.0;
    }

    private MilestoneReportAiRequest.ExpenseSummary
    buildEmptyExpenseSummary() {

        return MilestoneReportAiRequest
                .ExpenseSummary
                .builder()
                .startDate(null)
                .endDate(null)
                .totalExpense(0L)
                .dailyAverageExpense(0L)
                .categories(
                        buildEmptyCategories()
                )
                .build();
    }

    private MilestoneReportAiRequest.ExpenseSummary
    buildEmptyExpenseSummary(
            LocalDate startDate,
            LocalDate endDate
    ) {

        return MilestoneReportAiRequest
                .ExpenseSummary
                .builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalExpense(0L)
                .dailyAverageExpense(0L)
                .categories(
                        buildEmptyCategories()
                )
                .build();
    }

    private List<MilestoneReportAiRequest.CategoryExpense>
    buildEmptyCategories() {

        List<MilestoneReportAiRequest.CategoryExpense>
                categories =
                new ArrayList<>();

        for (ExpenseCategory category :
                ExpenseCategory.values()) {

            categories.add(
                    MilestoneReportAiRequest
                            .CategoryExpense
                            .builder()
                            .category(
                                    category.getDisplayName()
                            )
                            .amount(0L)
                            .proportion(0.0)
                            .changeRate(null)
                            .build()
            );
        }

        return categories;
    }

    private LocalDate resolveReportEndDate(
            Goal goal,
            Milestone milestone
    ) {

        if (milestone != null
                && milestone.getAchievedAt() != null) {

            return milestone
                    .getAchievedAt()
                    .toLocalDate();
        }

        return calculateTargetDate(
                goal,
                milestone == null
                        ? null
                        : milestone.getMilestonePercentage()
        );
    }

    private LocalDate calculateTargetDate(
            Goal goal,
            Integer percentage
    ) {

        if (goal == null
                || goal.getStartDate() == null
                || goal.getGoalDate() == null
                || percentage == null) {

            return goal == null
                    ? null
                    : goal.getStartDate();
        }

        long totalDays =
                ChronoUnit.DAYS.between(
                        goal.getStartDate(),
                        goal.getGoalDate()
                );

        if (totalDays <= 0L) {
            return goal.getStartDate();
        }

        int normalizedPercentage =
                Math.max(
                        0,
                        Math.min(
                                100,
                                percentage
                        )
                );

        long offsetDays =
                Math.round(
                        totalDays
                                * normalizedPercentage
                                / 100.0
                );

        offsetDays =
                Math.max(
                        0L,
                        Math.min(
                                totalDays,
                                offsetDays
                        )
                );

        return goal.getStartDate()
                .plusDays(offsetDays);
    }

    private String buildPrompt(
            MilestoneReportAiRequest request
    ) throws JsonProcessingException {

        String requestJson =
                objectMapper.writeValueAsString(
                        request
                );

        return """
                당신은 금융 목표 달성 리포트를 작성하는 AI 코치입니다.

                아래 목표, 마일스톤, 지출 데이터를 기반으로
                사용자의 목표 관리 상태를 분석해주세요.

                반드시 다음 규칙을 지켜주세요.

                1. 한국어로 작성하세요.
                2. 제목은 짧고 명확하게 작성하세요.
                3. 본문은 3~6문장으로 작성하세요.
                4. 반드시 JSON 형식으로만 응답하세요.
                5. 응답 형식:
                   {"title":"...","content":"..."}

                6. amount는 현재 분석 기간의
                   해당 카테고리 총지출입니다.

                7. proportion은 현재 분석 기간의
                   전체 지출 중 해당 카테고리의 비율입니다.

                8. dailyAverageExpense는 현재 분석 기간의
                   전체 지출 일평균입니다.

                9. changeRate는 총지출이 아니라
                   카테고리별 일평균 지출 기준의 증감률입니다.

                10. 현재 기간과 이전 비교 기간의 길이가
                    서로 다를 수 있습니다.
                    따라서 총지출만 비교해서
                    증가 또는 감소를 판단하지 마세요.

                11. 이전 비교 기간이 없으면
                    changeRate는 null입니다.

                12. changeRate가 null인 경우
                    임의의 증감률을 만들지 마세요.

                13. 현재 일평균 지출이 이전보다 낮다면
                    지출 관리가 개선된 것으로 해석할 수 있습니다.

                14. 현재 일평균 지출이 이전보다 높다면
                    증가한 카테고리를 구체적으로 언급할 수 있습니다.

                15. amount가 0인 카테고리는
                    해당 기간에 지출이 없었던 것입니다.

                16. 제공된 데이터에 없는 수치나 사실을
                    임의로 만들어내지 마세요.

                17. 같은 날짜에 여러 마일스톤이 달성된 경우
                    각각 별도의 리포트를 생성합니다.

                18. 단, 같은 날짜에 달성된 마일스톤들은
                    지출 분석 구간을 나누는 경계로 사용하지 않습니다.

                19. 지출 분석 기간은 시작일과 종료일을
                    모두 포함합니다.

                20. 이전 비교 기간이 존재하지 않는 경우
                    현재 기간의 지출 데이터를 중심으로 분석하세요.

                21. 하나의 마일스톤에 대해 리포트는 한 번만 생성됩니다.

                요청 데이터:
                """ + requestJson;
    }

    private ParsedReport parseAiResponse(
            String response
    ) {

        if (!StringUtils.hasText(response)) {
            throw new IllegalArgumentException(
                    "AI 응답이 비어 있습니다."
            );
        }

        String cleanedResponse =
                response
                        .replace("```json", "")
                        .replace("```", "")
                        .trim();

        try {

            JsonNode jsonNode =
                    objectMapper.readTree(
                            cleanedResponse
                    );

            String title =
                    jsonNode
                            .path("title")
                            .asText();

            String content =
                    jsonNode
                            .path("content")
                            .asText();

            if (!StringUtils.hasText(title)) {
                title = "마일스톤 AI 리포트";
            }

            if (!StringUtils.hasText(content)) {
                throw new IllegalArgumentException(
                        "AI 응답의 content가 비어 있습니다."
                );
            }

            return new ParsedReport(
                    title.trim(),
                    content.trim()
            );

        } catch (JsonProcessingException exception) {

            log.warn(
                    "AI 응답 JSON 파싱 실패. response={}",
                    cleanedResponse
            );

            throw new IllegalArgumentException(
                    "AI 응답 형식이 올바르지 않습니다."
            );
        }
    }

    private record PreviousExpensePeriod(
            LocalDate startDate,
            LocalDate endDate,
            long days,
            List<ExpenseCategorySummaryResponse> summaries
    ) {

        private static PreviousExpensePeriod empty() {

            return new PreviousExpensePeriod(
                    null,
                    null,
                    0L,
                    List.of()
            );
        }
    }

    private record ParsedReport(
            String title,
            String content
    ) {
    }
}