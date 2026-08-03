package org.jejuro.miraero.domain.pacemaker.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.pacemaker.domain.AutoSaving;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerResponse;
import org.jejuro.miraero.domain.pacemaker.mapper.PaceMakerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaceMakerImpl implements PaceMakerService {

  private final PaceMakerMapper paceMakerMapper;

  @Override
  public PaceMakerResponse getPaceMaker(Long userId) {
    AutoSaving autoSaving = paceMakerMapper.findByUserId(userId);
    return PaceMakerResponse.from(autoSaving);
  }
}
