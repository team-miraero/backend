package org.jejuro.miraero.domain.pacemaker.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.jejuro.miraero.domain.pacemaker.domain.AutoSaving;

@Getter
@Builder
public class PaceMakerResponse {

  private Long autoSavingId;
  private String status;
  private boolean enabled;
  
  public static PaceMakerResponse from(AutoSaving autoSaving) {
    if (autoSaving == null) {
      return PaceMakerResponse.builder()
          .autoSavingId(null)
          .status(null)
          .enabled(false)
          .build();
    }

    return PaceMakerResponse.builder()
        .autoSavingId(autoSaving.getAutoSavingId())
        .status(autoSaving.getAutoSavingStatus())
        .enabled("ACTIVE".equals(autoSaving.getAutoSavingStatus()))
        .build();
  }
}
