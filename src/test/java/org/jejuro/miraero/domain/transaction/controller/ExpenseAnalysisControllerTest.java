package org.jejuro.miraero.domain.transaction.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import org.jejuro.miraero.domain.transaction.dto.response.CategoryMonthChangeResponse;
import org.jejuro.miraero.domain.transaction.dto.response.CategoryThreeMonthAverageResponse;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseDashboardResponse;
import org.jejuro.miraero.domain.transaction.service.ExpenseAnalysisService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.exception.GlobalExceptionHandler;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.jejuro.miraero.global.security.JwtAuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;

@ExtendWith(MockitoExtension.class)
class ExpenseAnalysisControllerTest {
    private static final Long USER_ID = 42L;
    @Mock private ExpenseAnalysisService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ExpenseAnalysisController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(new AuthenticatedUser(USER_ID)));
    }

    @Test
    void getDashboard_successAndEmptyData() throws Exception {
        given(service.getDashboard(USER_ID, 2026, 7)).willReturn(new ExpenseDashboardResponse(2026, 7, Collections.emptyList(), new CategoryThreeMonthAverageResponse("2026-04", "2026-06", Collections.emptyList())));
        mockMvc.perform(get("/api/expense-analysis/dashboard").param("year", "2026").param("month", "7"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.recentTransactions").isEmpty())
                .andExpect(jsonPath("$.data.categoryThreeMonthAverages.startMonth").value("2026-04"));
        verify(service).getDashboard(USER_ID, 2026, 7);
    }

    @Test
    void getDashboard_returnsCategoryMonthChanges() throws Exception {
        given(service.getDashboard(USER_ID, 2026, 7)).willReturn(new ExpenseDashboardResponse(
                2026,
                7,
                Collections.emptyList(),
                new CategoryThreeMonthAverageResponse("2026-04", "2026-06", Collections.emptyList()),
                Collections.singletonList(new CategoryMonthChangeResponse(1L, "food", 250000L, 280000L, 30000L))
        ));

        mockMvc.perform(get("/api/expense-analysis/dashboard").param("year", "2026").param("month", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryMonthChanges[0].categoryId").value(1))
                .andExpect(jsonPath("$.data.categoryMonthChanges[0].previousMonthAmount").value(250000))
                .andExpect(jsonPath("$.data.categoryMonthChanges[0].currentMonthAmount").value(280000))
                .andExpect(jsonPath("$.data.categoryMonthChanges[0].changeAmount").value(30000));
    }

    @Test
    void getDashboard_invalidMonthReturnsGlobalError() throws Exception {
        given(service.getDashboard(eq(USER_ID), eq(2026), eq(13))).willThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE));
        mockMvc.perform(get("/api/expense-analysis/dashboard").param("year", "2026").param("month", "13"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("COMMON_002"));
    }

    @Test
    void getDashboard_requiresYearAndMonth() throws Exception {
        given(service.getDashboard(USER_ID, 2026, null)).willThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE));
        mockMvc.perform(get("/api/expense-analysis/dashboard").param("year", "2026")).andExpect(status().isBadRequest());
    }
}
