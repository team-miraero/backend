package org.jejuro.miraero.domain.pacemaker.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerHistorySearchCondition;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerMaxAmountUpdateRequest;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerStatusUpdateRequest;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDashboardResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalListResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerHistoryResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerMaxAmountUpdateResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerResponse;
import org.jejuro.miraero.domain.pacemaker.service.PaceMakerService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.response.PageResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pace-maker")
public class PaceMakerController {

  private final PaceMakerService paceMakerService;

  @GetMapping
  public ResponseEntity<ApiResponse<PaceMakerResponse>> getPaceMaker(
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    PaceMakerResponse response = paceMakerService.getPaceMaker(user.getUserId());

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PatchMapping("/{autoSavingId}/status")
  public ResponseEntity<ApiResponse<PaceMakerResponse>> updatePaceMaker(
      @PathVariable Long autoSavingId,
      @Valid @RequestBody PaceMakerStatusUpdateRequest request,
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    PaceMakerResponse response = paceMakerService.updateStatus(
        user.getUserId(),
        autoSavingId,
        request.getStatus()
    );

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/dashboard")
  public ResponseEntity<ApiResponse<PaceMakerDashboardResponse>> getDashboard(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestParam(defaultValue = "false") boolean includeStreak
  ) {
    PaceMakerDashboardResponse response =
        paceMakerService.getDashboard(user.getUserId(), includeStreak);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PatchMapping("/{autoSavingId}/max-amount")
  public ResponseEntity<ApiResponse<PaceMakerMaxAmountUpdateResponse>> updateMaxAmount(
      @PathVariable Long autoSavingId,
      @Valid @RequestBody PaceMakerMaxAmountUpdateRequest request,
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    PaceMakerMaxAmountUpdateResponse response = paceMakerService.updateMaxAmount(
        user.getUserId(),
        autoSavingId,
        request.getMaxAmount()
    );

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/histories")
  public ResponseEntity<ApiResponse<PageResponse<PaceMakerHistoryResponse>>> getHistories(
      @ModelAttribute PaceMakerHistorySearchCondition condition,
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    PageResponse<PaceMakerHistoryResponse> response = paceMakerService.getHistories(
        user.getUserId(),
        condition
    );

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/goals")
  public ResponseEntity<ApiResponse<PaceMakerGoalListResponse>> getPaceMakerGoals(
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    PaceMakerGoalListResponse response = paceMakerService.getPaceMakerGoals(user.getUserId());

    return ResponseEntity.ok(ApiResponse.success(response));
  }

}
