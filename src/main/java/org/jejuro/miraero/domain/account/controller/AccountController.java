package org.jejuro.miraero.domain.account.controller;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.account.dto.response.AccountListResponse;
import org.jejuro.miraero.domain.account.service.AccountService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

  private final AccountService accountService;

  @GetMapping
  public ResponseEntity<ApiResponse<AccountListResponse>> getAccounts(
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    AccountListResponse response = accountService.getAccounts(user.getUserId());
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
