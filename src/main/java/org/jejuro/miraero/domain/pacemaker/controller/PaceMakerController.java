package org.jejuro.miraero.domain.pacemaker.controller;

import javax.validation.Valid;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerGoalDepositRequest;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerHistorySearchCondition;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerMaxAmountUpdateRequest;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerStatusUpdateRequest;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDashboardResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalDepositResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pace-maker")
@Api(tags = "페이스메이커")
public class PaceMakerController {

  private final PaceMakerService paceMakerService;

  @GetMapping
  @ApiOperation(value = "페이스메이커 조회", notes = "로그인 사용자의 자동 저축 페이스메이커 설정을 조회합니다.")
  public ResponseEntity<ApiResponse<PaceMakerResponse>> getPaceMaker(
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    PaceMakerResponse response = paceMakerService.getPaceMaker(user.getUserId());

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PatchMapping("/{autoSavingId}/status")
  @ApiOperation(value = "페이스메이커 상태 변경", notes = "자동 저축 페이스메이커의 활성 상태를 변경합니다.")
  public ResponseEntity<ApiResponse<PaceMakerResponse>> updatePaceMaker(
      @ApiParam(value = "자동 저축 ID", example = "1", required = true) @PathVariable Long autoSavingId,
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
  @ApiOperation(value = "페이스메이커 대시보드 조회", notes = "오늘의 저축, 머니박스 잔액, 월간 성공 현황을 조회합니다. includeStreak=true이면 연속 저축 기록도 포함합니다.")
  public ResponseEntity<ApiResponse<PaceMakerDashboardResponse>> getDashboard(
      @AuthenticationPrincipal AuthenticatedUser user,
      @ApiParam(value = "연속 저축 기록 포함 여부", example = "false") @RequestParam(defaultValue = "false") boolean includeStreak
  ) {
    PaceMakerDashboardResponse response =
        paceMakerService.getDashboard(user.getUserId(), includeStreak);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PatchMapping("/{autoSavingId}/max-amount")
  @ApiOperation(value = "페이스메이커 최대 저축 금액 변경", notes = "자동 저축 1회 최대 금액을 변경합니다.")
  public ResponseEntity<ApiResponse<PaceMakerMaxAmountUpdateResponse>> updateMaxAmount(
      @ApiParam(value = "자동 저축 ID", example = "1", required = true) @PathVariable Long autoSavingId,
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
  @ApiOperation(value = "페이스메이커 저축 이력 조회", notes = "자동 저축 이력을 조건과 페이지 기준으로 조회합니다.")
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
  @ApiOperation(value = "페이스메이커 연결 목표 조회", notes = "페이스메이커로 저축할 수 있는 목표 목록을 조회합니다.")
  public ResponseEntity<ApiResponse<PaceMakerGoalListResponse>> getPaceMakerGoals(
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    PaceMakerGoalListResponse response = paceMakerService.getPaceMakerGoals(user.getUserId());

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PostMapping("/deposits")
  @ApiOperation(value = "목표에 저축금 입금", notes = "페이스메이커를 통해 선택한 목표에 저축금을 입금합니다.")
  public ResponseEntity<ApiResponse<PaceMakerGoalDepositResponse>> depositToGoal(
      @Valid @RequestBody PaceMakerGoalDepositRequest request,
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    PaceMakerGoalDepositResponse response = paceMakerService.depositToGoal(
        user.getUserId(),
        request
    );

    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
