package org.jejuro.miraero.domain.pacemaker.service;

import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerResponse;

public interface PaceMakerService {

  PaceMakerResponse getPaceMaker(Long userId);
}
