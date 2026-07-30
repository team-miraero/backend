package org.jejuro.miraero.domain.transaction.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.transaction.domain.CategoryExpenseQueryResult;
import org.jejuro.miraero.domain.transaction.domain.RecentTransactionQueryResult;
import org.jejuro.miraero.domain.transaction.dto.request.ExpenseAnalysisSearchCondition;
import org.jejuro.miraero.domain.transaction.dto.response.CategoryExpenseItemResponse;
import org.jejuro.miraero.domain.transaction.dto.response.CategoryExpenseSummaryResponse;
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
    private static final int RATIO_SCALE = 2;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final ExpenseAnalysisMapper expenseAnalysisMapper;

    @Override
    @Transactional(readOnly = true)
    public ExpenseDashboardResponse getDashboard(Long userId, Integer year, Integer month) {
        validate(userId, year, month);
        ExpenseAnalysisSearchCondition condition = createCondition(year, month);
        long totalExpense = expenseAnalysisMapper.sumTotalExpenses(userId, condition);

        return new ExpenseDashboardResponse(
                year,
                month,
                toRecentTransactions(expenseAnalysisMapper.findRecentExpenses(userId, condition)),
                new CategoryExpenseSummaryResponse(
                        totalExpense,
                        toCategoryExpenses(expenseAnalysisMapper.findCategoryExpenses(userId, condition), totalExpense)
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
        condition.setDateRange(yearMonth.atDay(1).atStartOfDay(), yearMonth.plusMonths(1).atDay(1).atStartOfDay());
        return condition;
    }

    private List<RecentTransactionResponse> toRecentTransactions(List<RecentTransactionQueryResult> results) {
        return results.stream().map(result -> new RecentTransactionResponse(result.getTransactionId(), result.getTransactionName(), result.getCategoryId(), result.getCategoryName(), result.getAmount(), result.getTransactedAt())).collect(Collectors.toList());
    }

    private List<CategoryExpenseItemResponse> toCategoryExpenses(List<CategoryExpenseQueryResult> results, long totalExpense) {
        return results.stream().map(result -> new CategoryExpenseItemResponse(result.getCategoryId(), result.getCategoryName(), result.getAmount(), calculateRatio(result.getAmount(), totalExpense))).collect(Collectors.toList());
    }

    private BigDecimal calculateRatio(Long amount, long totalExpense) {
        if (totalExpense == 0) {
            return BigDecimal.ZERO.setScale(RATIO_SCALE);
        }
        return BigDecimal.valueOf(amount).multiply(HUNDRED).divide(BigDecimal.valueOf(totalExpense), RATIO_SCALE, RoundingMode.HALF_UP);
    }
}
