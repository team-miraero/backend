package org.jejuro.miraero.domain.pacemaker.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaceMakerErrorCode implements ErrorCode {

  NOT_REGISTERED(
      HttpStatus.BAD_REQUEST,
      "PACEMAKER_001",
      "Pace maker is not registered."
  ),

  INSUFFICIENT_BALANCE(
      HttpStatus.BAD_REQUEST,
      "PACEMAKER_002",
      "Pace maker balance is insufficient."
  ),

  GOAL_WITHDRAWAL_ACCOUNT_NOT_CONNECTED(
      HttpStatus.BAD_REQUEST,
      "PACEMAKER_003",
      "Goal withdrawal account is not connected."
  ),

  INVALID_DEPOSIT_AMOUNT(
      HttpStatus.BAD_REQUEST,
      "PACEMAKER_004",
      "Deposit amount must be greater than zero."
  ),

  FORBIDDEN_GOAL_ACCOUNT(
      HttpStatus.FORBIDDEN,
      "PACEMAKER_005",
      "Account is not connected to user's goal."
  );

  private final HttpStatus status;
  private final String code;
  private final String message;
}
