package org.jejuro.miraero.domain.pacemaker.service;

import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerHistorySearchCondition;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDashboardResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerHistoryResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerMaxAmountUpdateResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerResponse;
import org.jejuro.miraero.global.response.PageResponse;

public interface PaceMakerService {

  PaceMakerResponse getPaceMaker(Long userId);

  PaceMakerResponse updateStatus(Long userId, Long autoSavingId, String status);

  PaceMakerDashboardResponse getDashboard(Long userId, boolean includeStreak);

  PaceMakerMaxAmountUpdateResponse updateMaxAmount(Long userId, Long autoSavingId, Long maxAmount);

  PageResponse<PaceMakerHistoryResponse> getHistories(
      Long userId,
      PaceMakerHistorySearchCondition condition
  );
}
