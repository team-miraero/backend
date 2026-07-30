package org.jejuro.miraero.domain.transaction.controller;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.transaction.dto.request.TransactionSearchCondition;
import org.jejuro.miraero.domain.transaction.dto.response.TransactionPageResponse;
import org.jejuro.miraero.domain.transaction.service.TransactionService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<ApiResponse<TransactionPageResponse>> getTransactions(
            @ModelAttribute TransactionSearchCondition condition,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        TransactionPageResponse response = transactionService.getTransactions(user.getUserId(), condition);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
