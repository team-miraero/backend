package org.jejuro.miraero.domain.goal.milestone.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneReportAiRequest {

    private GoalInfo goal;
    private MilestoneInfo milestone;
    private ExpenseSummary expenseSummary;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalInfo {

        private String goalName;
        private Long goalAmount;
        private Long startAmount;
        private LocalDate startDate;
        private LocalDate goalDate;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MilestoneInfo {

        private Integer percentage;
        private Long milestoneAmount;
        private LocalDate targetDate;
        private LocalDate achievedAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseSummary {

        private LocalDate startDate;
        private LocalDate endDate;
        private Long totalExpense;
        private Long dailyAverageExpense;

        @Builder.Default
        private List<CategoryExpense> categories = new ArrayList<>();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryExpense {

        private String category;
        private Long amount;
        private Double proportion;
        private Double changeRate;
    }
}