package org.jejuro.miraero.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GoalErrorCode implements ErrorCode{

    // Goal
    GOAL_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "GOAL_001",
            "목표를 찾을 수 없습니다."
    );

    private final HttpStatus status;
    private final String Code;
    private final String message;
}
