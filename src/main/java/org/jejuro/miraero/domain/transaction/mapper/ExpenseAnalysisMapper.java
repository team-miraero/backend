package org.jejuro.miraero.domain.transaction.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.transaction.domain.CategoryThreeMonthExpenseQueryResult;
import org.jejuro.miraero.domain.transaction.domain.RecentTransactionQueryResult;
import org.jejuro.miraero.domain.transaction.dto.request.ExpenseAnalysisSearchCondition;

public interface ExpenseAnalysisMapper {
    List<RecentTransactionQueryResult> findRecentExpenses(@Param("userId") Long userId, @Param("condition") ExpenseAnalysisSearchCondition condition);
    List<CategoryThreeMonthExpenseQueryResult> findCategoryThreeMonthExpenses(@Param("userId") Long userId, @Param("condition") ExpenseAnalysisSearchCondition condition);
}
