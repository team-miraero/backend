package org.jejuro.miraero.domain.pacemaker.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.pacemaker.domain.AutoSaving;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDashboardResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDashboardSummaryResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerMaxAmountUpdateResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerMoneyBoxResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerTodaySavingResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerWeeklyStreakResponse;
import org.jejuro.miraero.domain.pacemaker.mapper.PaceMakerMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaceMakerServiceImpl implements PaceMakerService {

  private final PaceMakerMapper paceMakerMapper;

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

}
