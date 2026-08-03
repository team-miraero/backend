package org.jejuro.miraero.domain.pacemaker.controller;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerResponse;
import org.jejuro.miraero.domain.pacemaker.service.PaceMakerService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

}
