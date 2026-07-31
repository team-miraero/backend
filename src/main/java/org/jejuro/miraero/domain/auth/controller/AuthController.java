package org.jejuro.miraero.domain.auth.controller;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.auth.dto.request.LoginRequest;
import org.jejuro.miraero.domain.auth.dto.request.SignUpRequest;
import org.jejuro.miraero.domain.auth.dto.response.LoginResponse;
import org.jejuro.miraero.domain.auth.dto.response.SignUpResponse;
import org.jejuro.miraero.domain.auth.dto.response.TokenReissueResponse;
import org.jejuro.miraero.domain.auth.service.AuthService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
  private static final String REFRESH_TOKEN_COOKIE_PATH = "/api/auth";
  private static final String SAME_SITE_POLICY = "Lax";

  private final AuthService authService;

  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<SignUpResponse>> signUp(
      @Valid @RequestBody SignUpRequest request
  ) {
    SignUpResponse response = authService.signUp(request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success(response));
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletResponse httpServletResponse
  ) {
    LoginResponse response = authService.login(request);

    addRefreshTokenCookie(
        httpServletResponse,
        response.getRefreshToken(),
        response.getRefreshTokenExpiresIn()
    );

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PostMapping("/reissue")
  public ResponseEntity<ApiResponse<TokenReissueResponse>> reissue(
      @CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
      HttpServletResponse httpServletResponse
  ) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
    }

    TokenReissueResponse response = authService.reissue(refreshToken);

    addRefreshTokenCookie(
        httpServletResponse,
        response.getRefreshToken(),
        response.getRefreshTokenExpiresIn()
    );

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  private void addRefreshTokenCookie(
      HttpServletResponse httpServletResponse,
      String refreshToken,
      Long refreshTokenExpiresIn
  ) {
    ResponseCookie refreshTokenCookie = ResponseCookie.from(
            REFRESH_TOKEN_COOKIE_NAME,
            refreshToken
        )
        .httpOnly(true)
        .secure(false)
        .path(REFRESH_TOKEN_COOKIE_PATH)
        .maxAge(refreshTokenExpiresIn)
        .sameSite(SAME_SITE_POLICY)
        .build();

    httpServletResponse.addHeader(
        HttpHeaders.SET_COOKIE,
        refreshTokenCookie.toString()
    );
  }

}
