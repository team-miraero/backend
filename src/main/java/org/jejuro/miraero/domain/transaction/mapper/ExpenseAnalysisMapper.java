package org.jejuro.miraero.domain.transaction.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.transaction.domain.CategoryExpenseQueryResult;
import org.jejuro.miraero.domain.transaction.domain.RecentTransactionQueryResult;
import org.jejuro.miraero.domain.transaction.dto.request.ExpenseAnalysisSearchCondition;

public interface ExpenseAnalysisMapper {
    List<RecentTransactionQueryResult> findRecentExpenses(@Param("userId") Long userId, @Param("condition") ExpenseAnalysisSearchCondition condition);
    List<CategoryExpenseQueryResult> findCategoryExpenses(@Param("userId") Long userId, @Param("condition") ExpenseAnalysisSearchCondition condition);
    long sumTotalExpenses(@Param("userId") Long userId, @Param("condition") ExpenseAnalysisSearchCondition condition);
}
