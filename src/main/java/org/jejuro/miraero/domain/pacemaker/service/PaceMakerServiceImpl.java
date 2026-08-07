package org.jejuro.miraero.domain.pacemaker.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBox;
import org.jejuro.miraero.domain.moneybox.mapper.MoneyBoxMapper;
import org.jejuro.miraero.domain.pacemaker.domain.AutoSaving;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerGoalDepositRequest;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerHistorySearchCondition;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDashboardResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDashboardSummaryResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDepositAssetResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalDepositAssetRowResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalDepositResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalListResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalSummaryResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalWithdrawalAccountRowResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerHistoryResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerMaxAmountUpdateResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerMoneyBoxResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerTodaySavingResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerWeeklyStreakResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerWithdrawalAccountResponse;
import org.jejuro.miraero.domain.pacemaker.exception.PaceMakerErrorCode;
import org.jejuro.miraero.domain.pacemaker.mapper.PaceMakerMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.response.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaceMakerServiceImpl implements PaceMakerService {

  private final PaceMakerMapper paceMakerMapper;
  private final MoneyBoxMapper moneyBoxMapper;
  private final AccountMapper accountMapper;

  @Override
  public PaceMakerResponse getPaceMaker(Long userId) {
    AutoSaving autoSaving = paceMakerMapper.findByUserId(userId);
    return PaceMakerResponse.from(autoSaving);
  }

  @Override
  @Transactional
  public PaceMakerResponse updateStatus(Long userId, Long autoSavingId, String status) {
    int updatedCount = paceMakerMapper.updateStatus(userId, autoSavingId, status);

    if (updatedCount == 0) {
      throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
    }

    AutoSaving autoSaving = paceMakerMapper.findByUserId(userId);
    return PaceMakerResponse.from(autoSaving);
  }

  @Override
  @Transactional(readOnly = true)
  public PaceMakerDashboardResponse getDashboard(Long userId, boolean includeStreak) {
    PaceMakerDashboardSummaryResponse summary = paceMakerMapper.findDashboardByUserId(userId);

    if (summary == null) {
      throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
    }

    Integer currentStreak = null;
    List<PaceMakerWeeklyStreakResponse> weeklyStreak = null;
    Integer monthlySuccessCount = null;

    if (includeStreak) {
      Long autoSavingId = summary.getAutoSavingId();

      currentStreak = calculateCurrentStreak(
          paceMakerMapper.findRecentSavingDates(autoSavingId)
      );
      weeklyStreak = paceMakerMapper.findWeeklyStreak(autoSavingId);
      monthlySuccessCount = paceMakerMapper.countMonthlySuccess(autoSavingId);
    }

    return PaceMakerDashboardResponse.builder()
        .autoSavingId(summary.getAutoSavingId())
        .status(summary.getStatus())
        .maxAmount(summary.getMaxAmount())
        .moneyBox(PaceMakerMoneyBoxResponse.builder()
            .moneyBoxId(summary.getMoneyBoxId())
            .balance(summary.getBalance())
            .maskedAccountNumber(summary.getMaskedAccountNumber())
            .build())
        .todaySaving(PaceMakerTodaySavingResponse.builder()
            .saved(Boolean.TRUE.equals(summary.getTodaySaved()))
            .amount(summary.getTodaySavingAmount())
            .build())
        .currentStreak(currentStreak)
        .weeklyStreak(weeklyStreak)
        .monthlySuccessCount(monthlySuccessCount)
        .build();
  }

  @Override
  @Transactional
  public PaceMakerMaxAmountUpdateResponse updateMaxAmount(Long userId, Long autoSavingId,
      Long maxAmount) {
    int updateCount = paceMakerMapper.updateMaxAmount(userId, autoSavingId, maxAmount);

    if (updateCount == 0) {
      throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
    }

    return PaceMakerMaxAmountUpdateResponse.builder()
        .autoSavingId(autoSavingId)
        .maxAmount(maxAmount)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<PaceMakerHistoryResponse> getHistories(
      Long userId,
      PaceMakerHistorySearchCondition condition
  ) {
    condition.validate();

    LocalDateTime startDateTime = LocalDate.now()
        .withDayOfMonth(1)
        .atStartOfDay();

    LocalDateTime endDateTime = startDateTime.plusMonths(1);

    List<PaceMakerHistoryResponse> histories = paceMakerMapper.findHistories(
        userId,
        startDateTime,
        endDateTime,
        condition.getOffset(),
        condition.getSize()
    );

    long totalElements = paceMakerMapper.countHistories(
        userId,
        startDateTime,
        endDateTime
    );

    return PageResponse.of(
        histories,
        condition.getPage(),
        condition.getSize(),
        totalElements
    );
  }

  private int calculateCurrentStreak(List<LocalDate> savingDates) {
    if (savingDates == null || savingDates.isEmpty()) {
      return 0;
    }

    LocalDate expectedDate = LocalDate.now();

    if (!savingDates.get(0).equals(expectedDate)) {
      expectedDate = expectedDate.minusDays(1);
    }

    int streak = 0;

    for (LocalDate savingDate : savingDates) {
      if (!savingDate.equals(expectedDate)) {
        break;
      }

      streak++;
      expectedDate = expectedDate.minusDays(1);
    }

    return streak;
  }

  @Override
  @Transactional(readOnly = true)
  public PaceMakerGoalListResponse getPaceMakerGoals(Long userId) {
    AutoSaving autoSaving = paceMakerMapper.findByUserId(userId);

    if (autoSaving == null) {
      throw new BusinessException(PaceMakerErrorCode.NOT_REGISTERED);
    }

    //목표, 입금, 출금계좌 조회
    List<PaceMakerGoalSummaryResponse> goals = paceMakerMapper.findPaceMakerGoals(userId);
    List<PaceMakerGoalDepositAssetRowResponse> depositAssetRows =
        paceMakerMapper.findPaceMakerGoalDepositAssets(userId);
    List<PaceMakerGoalWithdrawalAccountRowResponse> withdrawalAccountRows =
        paceMakerMapper.findPaceMakerGoalWithdrawalAccounts(userId);

    //goalId 기준으로 응답을 조립
    Map<Long, List<PaceMakerGoalDepositAssetRowResponse>> depositAssetsByGoalId =
        depositAssetRows.stream()
            .collect(Collectors.groupingBy(PaceMakerGoalDepositAssetRowResponse::getGoalId));

    Map<Long, List<PaceMakerGoalWithdrawalAccountRowResponse>> withdrawalAccountsByGoalId =
        withdrawalAccountRows.stream()
            .collect(Collectors.groupingBy(PaceMakerGoalWithdrawalAccountRowResponse::getGoalId));

    //최종 응답 조립
    List<PaceMakerGoalResponse> goalResponses = goals.stream()
        .map(goal -> {
          List<PaceMakerDepositAssetResponse> depositAssets =
              depositAssetsByGoalId
                  .getOrDefault(goal.getGoalId(), Collections.emptyList())
                  .stream()
                  .map(asset -> PaceMakerDepositAssetResponse.builder()
                      .assetType(asset.getAssetType())
                      .assetId(asset.getAssetId())
                      .financialInstitutionName(asset.getFinancialInstitutionName())
                      .maskedAccountNumber(asset.getMaskedAccountNumber())
                      .balance(asset.getBalance())
                      .build())
                  .collect(Collectors.toList());

          Long totalSavedAmount = depositAssets.stream()
              .map(PaceMakerDepositAssetResponse::getBalance)
              .filter(balance -> balance != null)
              .reduce(0L, Long::sum);

          List<PaceMakerWithdrawalAccountResponse> withdrawalAccounts =
              withdrawalAccountsByGoalId
                  .getOrDefault(goal.getGoalId(), Collections.emptyList())
                  .stream()
                  .map(account -> PaceMakerWithdrawalAccountResponse.builder()
                      .accountId(account.getAccountId())
                      .financialInstitutionName(account.getFinancialInstitutionName())
                      .maskedAccountNumber(account.getMaskedAccountNumber())
                      .balance(account.getBalance())
                      .build())
                  .collect(Collectors.toList());

          return PaceMakerGoalResponse.builder()
              .goalId(goal.getGoalId())
              .goalName(goal.getGoalName())
              .goalType(goal.getGoalType())
              .goalAmount(goal.getGoalAmount())
              .totalSavedAmount(totalSavedAmount)
              .depositAssets(depositAssets)
              .withdrawalAccounts(withdrawalAccounts)
              .build();
        })
        .collect(Collectors.toList());

    return PaceMakerGoalListResponse.builder()
        .goals(goalResponses)
        .build();
  }

  @Override
  @Transactional
  public PaceMakerGoalDepositResponse depositToGoal(Long userId,
      PaceMakerGoalDepositRequest request) {
    if (request.getAmount() == null || request.getAmount() <= 0) {
      throw new BusinessException(PaceMakerErrorCode.INVALID_DEPOSIT_AMOUNT);
    }

    MoneyBox savingMoneyBox = moneyBoxMapper.findPaceMakerMoneyBoxByIdAndUserIdForUpdate(
        request.getMoneyBoxId(),
        userId
    );

    if (savingMoneyBox == null) {
      throw new BusinessException(PaceMakerErrorCode.NOT_REGISTERED);
    }

    boolean existsGoalDepositAccount =
        paceMakerMapper.existsGoalDepositAccountByUserIdAndAccountId(
            userId,
            request.getAccountId()
        );

    if (!existsGoalDepositAccount) {
      throw new BusinessException(PaceMakerErrorCode.FORBIDDEN_GOAL_ACCOUNT);
    }

    Long balance = savingMoneyBox.getBalance();

    if (balance < request.getAmount()) {
      throw new BusinessException(PaceMakerErrorCode.INSUFFICIENT_BALANCE);
    }

    moneyBoxMapper.decreaseBalance(
        savingMoneyBox.getMoneyBoxId(),
        userId,
        request.getAmount()
    );

    accountMapper.increaseBalance(
        request.getAccountId(),
        userId,
        request.getAmount()
    );

    return PaceMakerGoalDepositResponse.builder()
        .accountId(request.getAccountId())
        .depositedAmount(request.getAmount())
        .remainingBalance(balance - request.getAmount())
        .build();
  }
}
