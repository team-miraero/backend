package org.jejuro.miraero.domain.pacemaker.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class PaceMakerStatusUpdateRequest {

  @NotBlank(message = "변경할 자동저축 상태는 필수입니다.")
  @Pattern(regexp = "ACTIVE|PAUSED", message = "자동저축 상태는 ACTIVE 또는 PAUSED만 가능합니다.")
  private String status;
}
