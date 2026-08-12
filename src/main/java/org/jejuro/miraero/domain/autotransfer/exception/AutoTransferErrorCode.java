package org.jejuro.miraero.domain.autotransfer.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AutoTransferErrorCode implements ErrorCode {

    FUTURE_EXECUTION_DATE(
            HttpStatus.BAD_REQUEST,
            "AUTO_TRANSFER_001",
            "아직 오지 않은 날짜의 자동이체는 실행할 수 없습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
