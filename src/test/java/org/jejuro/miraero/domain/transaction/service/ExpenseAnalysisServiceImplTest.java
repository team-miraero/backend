package org.jejuro.miraero.domain.transaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.jejuro.miraero.domain.transaction.domain.CategoryThreeMonthExpenseQueryResult;
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

    @Mock
    private ExpenseAnalysisMapper mapper;

    @Test
    void getDashboard_returnsRecentTransactionsAndThreeMonthCategoryAverages() {
        ExpenseAnalysisService service = new ExpenseAnalysisServiceImpl(mapper);
        when(mapper.findRecentExpenses(eq(1L), any())).thenReturn(List.of(
                new RecentTransactionQueryResult(1L, "배달 음식", 1L, "식비", 150_000L, LocalDateTime.of(2026, 7, 15, 12, 30))
        ));
        when(mapper.findCategoryThreeMonthExpenses(eq(1L), any())).thenReturn(List.of(
                new CategoryThreeMonthExpenseQueryResult(1L, "식비", 660_000L)
        ));

        ExpenseDashboardResponse response = service.getDashboard(1L, 2026, 7);

        assertEquals("배달 음식", response.getRecentTransactions().get(0).getTransactionName());
        assertEquals("2026-04", response.getCategoryThreeMonthAverages().getStartMonth());
        assertEquals("2026-06", response.getCategoryThreeMonthAverages().getEndMonth());
        assertEquals(220_000L, response.getCategoryThreeMonthAverages().getCategories().get(0).getAverageMonthlyAmount());
    }

    @Test
    void getDashboard_usesCompletedThreeMonthRangeExcludingCurrentMonth() {
        ExpenseAnalysisService service = new ExpenseAnalysisServiceImpl(mapper);
        when(mapper.findRecentExpenses(eq(1L), any())).thenReturn(Collections.emptyList());
        when(mapper.findCategoryThreeMonthExpenses(eq(1L), any())).thenReturn(Collections.emptyList());

        service.getDashboard(1L, 2026, 7);

        ArgumentCaptor<ExpenseAnalysisSearchCondition> recentConditionCaptor =
                ArgumentCaptor.forClass(ExpenseAnalysisSearchCondition.class);
        ArgumentCaptor<ExpenseAnalysisSearchCondition> averageConditionCaptor =
                ArgumentCaptor.forClass(ExpenseAnalysisSearchCondition.class);
        verify(mapper).findRecentExpenses(eq(1L), recentConditionCaptor.capture());
        verify(mapper).findCategoryThreeMonthExpenses(eq(1L), averageConditionCaptor.capture());

        assertEquals(LocalDateTime.of(2026, 7, 1, 0, 0), recentConditionCaptor.getValue().getStartDateTime());
        assertEquals(LocalDateTime.of(2026, 8, 1, 0, 0), recentConditionCaptor.getValue().getEndDateTime());
        assertEquals(LocalDateTime.of(2026, 4, 1, 0, 0), averageConditionCaptor.getValue().getStartDateTime());
        assertEquals(LocalDateTime.of(2026, 7, 1, 0, 0), averageConditionCaptor.getValue().getEndDateTime());
    }

    @Test
    void getDashboard_alwaysDividesTotalByThreeWithTruncation() {
        ExpenseAnalysisService service = new ExpenseAnalysisServiceImpl(mapper);
        when(mapper.findRecentExpenses(eq(1L), any())).thenReturn(Collections.emptyList());
        when(mapper.findCategoryThreeMonthExpenses(eq(1L), any())).thenReturn(List.of(
                new CategoryThreeMonthExpenseQueryResult(1L, "식비", 100_000L)
        ));

        ExpenseDashboardResponse response = service.getDashboard(1L, 2026, 7);

        assertEquals(33_333L, response.getCategoryThreeMonthAverages().getCategories().get(0).getAverageMonthlyAmount());
    }

    @Test
    void getDashboard_returnsEmptyAverageCategoriesWhenNoPaymentExistsInPeriod() {
        ExpenseAnalysisService service = new ExpenseAnalysisServiceImpl(mapper);
        when(mapper.findRecentExpenses(eq(1L), any())).thenReturn(Collections.emptyList());
        when(mapper.findCategoryThreeMonthExpenses(eq(1L), any())).thenReturn(Collections.emptyList());

        ExpenseDashboardResponse response = service.getDashboard(1L, 2026, 1);

        assertEquals("2025-10", response.getCategoryThreeMonthAverages().getStartMonth());
        assertEquals("2025-12", response.getCategoryThreeMonthAverages().getEndMonth());
        assertEquals(0, response.getRecentTransactions().size());
        assertEquals(0, response.getCategoryThreeMonthAverages().getCategories().size());
    }

    @Test
    void getDashboard_rejectsInvalidMonth() {
        ExpenseAnalysisService service = new ExpenseAnalysisServiceImpl(mapper);

        assertThrows(BusinessException.class, () -> service.getDashboard(1L, 2026, 13));
    }
}
