package org.jejuro.miraero.domain.transaction.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.transaction.dto.request.ExpenseCategoryTargetSaveRequest;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseCategoryTargetListResponse;
import org.jejuro.miraero.domain.transaction.service.ExpenseCategoryTargetService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expense-category-targets")
public class ExpenseCategoryTargetController {

    private final ExpenseCategoryTargetService expenseCategoryTargetService;

    @GetMapping
    public ResponseEntity<ApiResponse<ExpenseCategoryTargetListResponse>> getTargets(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(expenseCategoryTargetService.getTargets(user.getUserId()))
        );
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ExpenseCategoryTargetListResponse>> saveTargets(
            @Valid @RequestBody ExpenseCategoryTargetSaveRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(expenseCategoryTargetService.saveTargets(user.getUserId(), request))
        );
    }
}
