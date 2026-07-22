package org.jejuro.miraero.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.jejuro.miraero.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 비즈니스 예외 처리
   */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusinessException(
      BusinessException exception
  ) {
    ErrorCode errorCode = exception.getErrorCode();

    return ResponseEntity
        .status(errorCode.getStatus())
        .body(ApiResponse.error(
            errorCode.getCode(),
            errorCode.getMessage()
        ));
  }


  /**
   * 처리되지 않은 서버 예외 처리
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(
      Exception exception
  ) {
    ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;

    log.error("Unhandled exception occurred", exception);

    return ResponseEntity
        .status(errorCode.getStatus())
        .body(ApiResponse.error(
            errorCode.getCode(),
            errorCode.getMessage()
        ));
  }
}
