package org.jejuro.miraero.domain.transaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.jejuro.miraero.domain.transaction.domain.CategoryExpenseQueryResult;
import org.jejuro.miraero.domain.transaction.domain.RecentTransactionQueryResult;
import org.jejuro.miraero.domain.transaction.dto.request.ExpenseAnalysisSearchCondition;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseDashboardResponse;
import org.jejuro.miraero.domain.transaction.mapper.ExpenseAnalysisMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpenseAnalysisServiceImplTest {
    @Mock private ExpenseAnalysisMapper mapper;

    @Test
    void getDashboard_assemblesDashboardAndDateRange() {
        ExpenseAnalysisService service = new ExpenseAnalysisServiceImpl(mapper);
        when(mapper.sumTotalExpenses(eq(1L), any())).thenReturn(320_000L);
        when(mapper.findRecentExpenses(eq(1L), any())).thenReturn(List.of(new RecentTransactionQueryResult(1L, "배달의민족", 1L, "식비", 150_000L, LocalDateTime.of(2026, 7, 15, 12, 30))));
        when(mapper.findCategoryExpenses(eq(1L), any())).thenReturn(List.of(new CategoryExpenseQueryResult(1L, "식비", 320_000L)));
        ExpenseDashboardResponse response = service.getDashboard(1L, 2026, 7);
        ArgumentCaptor<ExpenseAnalysisSearchCondition> captor = ArgumentCaptor.forClass(ExpenseAnalysisSearchCondition.class);
        org.mockito.Mockito.verify(mapper).sumTotalExpenses(eq(1L), captor.capture());
        assertEquals(LocalDateTime.of(2026, 7, 1, 0, 0), captor.getValue().getStartDateTime());
        assertEquals(LocalDateTime.of(2026, 8, 1, 0, 0), captor.getValue().getEndDateTime());
        assertEquals("배달의민족", response.getRecentTransactions().get(0).getTransactionName());
        assertEquals(320_000L, response.getCategoryExpenseSummary().getTotalExpense());
        assertEquals("100.00", response.getCategoryExpenseSummary().getCategories().get(0).getRatio().toPlainString());
    }

    @Test
    void getDashboard_returnsEmptyDataAndHandlesJanuary() {
        ExpenseAnalysisService service = new ExpenseAnalysisServiceImpl(mapper);
        when(mapper.sumTotalExpenses(eq(1L), any())).thenReturn(0L);
        when(mapper.findRecentExpenses(eq(1L), any())).thenReturn(Collections.emptyList());
        when(mapper.findCategoryExpenses(eq(1L), any())).thenReturn(Collections.emptyList());
        ExpenseDashboardResponse response = service.getDashboard(1L, 2026, 1);
        assertEquals(0L, response.getCategoryExpenseSummary().getTotalExpense());
        assertEquals(0, response.getRecentTransactions().size());
        assertEquals(0, response.getCategoryExpenseSummary().getCategories().size());
    }

    @Test
    void getDashboard_rejectsInvalidMonth() {
        ExpenseAnalysisService service = new ExpenseAnalysisServiceImpl(mapper);
        assertThrows(BusinessException.class, () -> service.getDashboard(1L, 2026, 13));
    }
}
