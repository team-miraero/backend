package org.jejuro.miraero.domain.goal.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GoalErrorCode implements ErrorCode {

    // Goal
    GOAL_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "GOAL_001",
            "목표를 찾을 수 없습니다."
    ),

    GOAL_NOT_COMPLETED(
            HttpStatus.BAD_REQUEST,
            "GOAL_002",
            "완료된 목표만 컬렉션에 저장할 수 있습니다."
    ),

    GOAL_ASSET_ALREADY_CONNECTED(
            HttpStatus.CONFLICT,
            "G003",
                    "이미 연결된 자산입니다."
    ),

    GOAL_ASSET_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "GOAL_004",
            "연결된 목표 자산을 찾을 수 없습니다."
    );


    private final HttpStatus status;
    private final String Code;
    private final String message;
}
