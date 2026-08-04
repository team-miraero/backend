package org.jejuro.miraero.domain.pacemaker.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.pacemaker.domain.AutoSaving;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerResponse;
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

}
