package org.jejuro.miraero.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.user.dto.response.ProfileResponse;
import org.jejuro.miraero.domain.user.service.UserService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/profile")
  public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    ProfileResponse response = userService.getProfile(user.getUserId());

    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
