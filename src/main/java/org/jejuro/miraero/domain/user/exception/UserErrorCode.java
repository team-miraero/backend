package org.jejuro.miraero.domain.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

  EMAIL_ALREADY_EXISTS(
      HttpStatus.CONFLICT,
      "USER_001",
      "이미 가입된 이메일입니다."
  );

  private final HttpStatus status;
  private final String code;
  private final String message;
}
