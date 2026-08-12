package org.jejuro.miraero.domain.pacemaker.dto.response;

import lombok.Builder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import org.jejuro.miraero.domain.pacemaker.domain.AutoSaving;

@Getter
@Builder
@ApiModel(description = "페이스메이커 설정 정보")
public class PaceMakerResponse {

  @ApiModelProperty(value = "자동 저축 ID. 미등록 시 null")
  private Long autoSavingId;
  @ApiModelProperty(value = "페이스메이커 등록 여부")
  private boolean registered;
  @ApiModelProperty(value = "자동 저축 상태. 미등록 시 null, 등록 시 ACTIVE 또는 PAUSED")
  private String status;
  @ApiModelProperty(value = "자동 저축 활성 여부")
  private boolean enabled;

  public static PaceMakerResponse from(AutoSaving autoSaving) {
    if (autoSaving == null) {
      return PaceMakerResponse.builder()
          .autoSavingId(null)
          .registered(false)
          .status(null)
          .enabled(false)
          .build();
    }

    return PaceMakerResponse.builder()
        .autoSavingId(autoSaving.getAutoSavingId())
        .registered(true)
        .status(autoSaving.getAutoSavingStatus())
        .enabled("ACTIVE".equals(autoSaving.getAutoSavingStatus()))
        .build();
  }
}
