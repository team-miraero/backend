package org.jejuro.miraero.domain.transaction.controller;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.transaction.dto.request.TransactionSearchCondition;
import org.jejuro.miraero.domain.transaction.dto.response.TransactionPageResponse;
import org.jejuro.miraero.domain.transaction.service.TransactionService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {

    // TODO JWT 인증 연동 후 SecurityContext에서 로그인 사용자 ID 조회
    private static final Long TEST_USER_ID = 1L;

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<ApiResponse<TransactionPageResponse>> getTransactions(
            @ModelAttribute TransactionSearchCondition condition
    ) {
        TransactionPageResponse response = transactionService.getTransactions(TEST_USER_ID, condition);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
