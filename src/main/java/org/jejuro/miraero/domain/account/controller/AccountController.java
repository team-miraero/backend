package org.jejuro.miraero.domain.account.controller;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.account.dto.response.AccountListResponse;
import org.jejuro.miraero.domain.account.dto.response.AccountResponse;
import org.jejuro.miraero.domain.account.service.AccountService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

  private final AccountService accountService;

  @GetMapping
  public ResponseEntity<ApiResponse<AccountListResponse>> getAccounts(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestParam(required = false) String accountType
  ) {
    AccountListResponse response = accountService.getAccounts(user.getUserId(), accountType);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/{accountId}")
  public ResponseEntity<ApiResponse<AccountResponse>> getAccount(
      @PathVariable Long accountId,
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    AccountResponse response = accountService.getAccount(accountId, user.getUserId());
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
