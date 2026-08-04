package org.jejuro.miraero.domain.pacemaker.mapper;

import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.pacemaker.domain.AutoSaving;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDashboardSummaryResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerWeeklyStreakResponse;

@Mapper
public interface PaceMakerMapper {

  AutoSaving findByUserId(@Param("userId") Long userId);

  int updateStatus(
      @Param("userId") Long userId,
      @Param("autoSavingId") Long autoSavingId,
      @Param("status") String status);

  int updateMaxAmount(
      @Param("userId") Long userId,
      @Param("autoSavingId") Long autoSavingId,
      @Param("maxAmount") Long maxAmount
  );

  PaceMakerDashboardSummaryResponse findDashboardByUserId(@Param("userId") Long userId);

  Integer countMonthlySuccess(@Param("autoSavingId") Long autoSavingId);

  List<PaceMakerWeeklyStreakResponse> findWeeklyStreak(@Param("autoSavingId") Long autoSavingId);

  List<LocalDate> findRecentSavingDates(@Param("autoSavingId") Long autoSavingId);


}
