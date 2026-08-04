package org.jejuro.miraero.domain.pacemaker.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.pacemaker.domain.AutoSaving;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDashboardSummaryResponse;

@Mapper
public interface PaceMakerMapper {

  AutoSaving findByUserId(@Param("userId") Long userId);

  int updateStatus(
      @Param("userId") Long userId,
      @Param("autoSavingId") Long autoSavingId,
      @Param("status") String status);

  PaceMakerDashboardSummaryResponse findDashboardByUserId(@Param("userId") Long userId);
}
