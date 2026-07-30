package org.jejuro.miraero.domain.transaction.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.jejuro.miraero.domain.transaction.dto.request.TransactionSearchCondition;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseCategoryResponse;
import org.jejuro.miraero.domain.transaction.dto.response.PaginationResponse;
import org.jejuro.miraero.domain.transaction.dto.response.TransactionPageResponse;
import org.jejuro.miraero.domain.transaction.dto.response.TransactionResponse;
import org.jejuro.miraero.domain.transaction.service.TransactionService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TransactionController transactionController = new TransactionController(transactionService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(transactionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("거래내역 조회 요청은 200 응답과 ApiResponse 형식을 반환한다")
    void getTransactions_success() throws Exception {
        TransactionPageResponse response = TransactionPageResponse.of(
                List.of(TransactionResponse.of(
                        1L,
                        "PAYMENT",
                        15_000L,
                        1_200_000L,
                        LocalDateTime.of(2026, 7, 30, 12, 30),
                        ExpenseCategoryResponse.of(1L, "식비")
                )),
                PaginationResponse.of(1, 10, 1L)
        );
        given(transactionService.getTransactions(eq(1L), any(TransactionSearchCondition.class)))
                .willReturn(response);

        mockMvc.perform(get("/api/transactions")
                        .param("year", "2026")
                        .param("month", "7")
                        .param("categoryId", "1")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactions[0].transactionId").value(1))
                .andExpect(jsonPath("$.data.transactions[0].category.categoryName").value("식비"))
                .andExpect(jsonPath("$.data.pagination.totalElements").value(1));

        ArgumentCaptor<TransactionSearchCondition> conditionCaptor =
                ArgumentCaptor.forClass(TransactionSearchCondition.class);
        verify(transactionService).getTransactions(eq(1L), conditionCaptor.capture());
        TransactionSearchCondition condition = conditionCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(2026, condition.getYear());
        org.junit.jupiter.api.Assertions.assertEquals(7, condition.getMonth());
        org.junit.jupiter.api.Assertions.assertEquals(1L, condition.getCategoryId());
        org.junit.jupiter.api.Assertions.assertEquals(1, condition.getPage());
        org.junit.jupiter.api.Assertions.assertEquals(10, condition.getSize());
    }

    @Test
    @DisplayName("거래내역이 비어 있어도 정상 응답을 반환한다")
    void getTransactions_emptyList() throws Exception {
        TransactionPageResponse response = TransactionPageResponse.of(
                Collections.emptyList(),
                PaginationResponse.of(1, 10, 0L)
        );
        given(transactionService.getTransactions(eq(1L), any(TransactionSearchCondition.class)))
                .willReturn(response);

        mockMvc.perform(get("/api/transactions")
                        .param("year", "2026")
                        .param("month", "7")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactions").isEmpty())
                .andExpect(jsonPath("$.data.pagination.totalElements").value(0));

        verify(transactionService).getTransactions(eq(1L), any(TransactionSearchCondition.class));
    }

    @Test
    @DisplayName("Service 입력값 예외는 전역 예외 처리 형식으로 반환한다")
    void getTransactions_invalidRequest() throws Exception {
        given(transactionService.getTransactions(eq(1L), any(TransactionSearchCondition.class)))
                .willThrow(new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE));

        mockMvc.perform(get("/api/transactions")
                        .param("year", "2026")
                        .param("month", "13")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_002"));

        verify(transactionService).getTransactions(eq(1L), any(TransactionSearchCondition.class));
    }
}
