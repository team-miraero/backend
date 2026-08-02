package org.jejuro.miraero.domain.transaction.service;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.transaction.domain.CategoryThreeMonthExpenseQueryResult;
import org.jejuro.miraero.domain.transaction.domain.RecentTransactionQueryResult;
import org.jejuro.miraero.domain.transaction.dto.request.ExpenseAnalysisSearchCondition;
import org.jejuro.miraero.domain.transaction.dto.response.CategoryThreeMonthAverageItemResponse;
import org.jejuro.miraero.domain.transaction.dto.response.CategoryThreeMonthAverageResponse;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseDashboardResponse;
import org.jejuro.miraero.domain.transaction.dto.response.RecentTransactionResponse;
import org.jejuro.miraero.domain.transaction.mapper.ExpenseAnalysisMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpenseAnalysisServiceImpl implements ExpenseAnalysisService {

    private static final int MIN_YEAR = 2000;
    private static final long THREE_MONTHS = 3L;

    private final ExpenseAnalysisMapper expenseAnalysisMapper;

    @Override
    @Transactional(readOnly = true)
    public ExpenseDashboardResponse getDashboard(Long userId, Integer year, Integer month) {
        validate(userId, year, month);
        ExpenseAnalysisSearchCondition recentTransactionCondition = createCondition(year, month);
        YearMonth referenceMonth = YearMonth.of(year, month);
        YearMonth startMonth = referenceMonth.minusMonths(THREE_MONTHS);
        ExpenseAnalysisSearchCondition threeMonthCondition = createCondition(startMonth, referenceMonth);

        return new ExpenseDashboardResponse(
                year,
                month,
                toRecentTransactions(expenseAnalysisMapper.findRecentExpenses(userId, recentTransactionCondition)),
                new CategoryThreeMonthAverageResponse(
                        startMonth.toString(),
                        referenceMonth.minusMonths(1).toString(),
                        toCategoryThreeMonthAverages(
                                expenseAnalysisMapper.findCategoryThreeMonthExpenses(userId, threeMonthCondition)
                        )
                )
        );
    }

    private void validate(Long userId, Integer year, Integer month) {
        if (userId == null || userId <= 0 || year == null || year < MIN_YEAR
                || year > YearMonth.now().getYear() + 1 || month == null || month < 1 || month > 12) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private ExpenseAnalysisSearchCondition createCondition(Integer year, Integer month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        ExpenseAnalysisSearchCondition condition = new ExpenseAnalysisSearchCondition(year, month);
        return createCondition(yearMonth, yearMonth.plusMonths(1), condition);
    }

    private ExpenseAnalysisSearchCondition createCondition(YearMonth startMonth, YearMonth endMonth) {
        ExpenseAnalysisSearchCondition condition = new ExpenseAnalysisSearchCondition(startMonth.getYear(), startMonth.getMonthValue());
        return createCondition(startMonth, endMonth, condition);
    }

    private ExpenseAnalysisSearchCondition createCondition(
            YearMonth startMonth,
            YearMonth endMonth,
            ExpenseAnalysisSearchCondition condition
    ) {
        condition.setDateRange(startMonth.atDay(1).atStartOfDay(), endMonth.atDay(1).atStartOfDay());
        return condition;
    }

    private List<RecentTransactionResponse> toRecentTransactions(List<RecentTransactionQueryResult> results) {
        return results.stream().map(result -> new RecentTransactionResponse(result.getTransactionId(), result.getTransactionName(), result.getCategoryId(), result.getCategoryName(), result.getAmount(), result.getTransactedAt())).collect(Collectors.toList());
    }

    private List<CategoryThreeMonthAverageItemResponse> toCategoryThreeMonthAverages(
            List<CategoryThreeMonthExpenseQueryResult> results
    ) {
        return results.stream()
                .map(result -> new CategoryThreeMonthAverageItemResponse(
                        result.getCategoryId(),
                        result.getCategoryName(),
                        result.getTotalAmount() / THREE_MONTHS
                ))
                .collect(Collectors.toList());
    }
}
