package org.jejuro.miraero.domain.pacemaker.service;

import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDashboardResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerResponse;

public interface PaceMakerService {

  PaceMakerResponse getPaceMaker(Long userId);

  PaceMakerResponse updateStatus(Long userId, Long autoSavingId, String status);

  PaceMakerDashboardResponse getDashboard(Long userId, boolean includeStreak);
}
