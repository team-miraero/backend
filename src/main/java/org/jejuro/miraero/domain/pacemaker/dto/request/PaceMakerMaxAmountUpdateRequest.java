package org.jejuro.miraero.domain.pacemaker.dto.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class PaceMakerMaxAmountUpdateRequest {

  @NotNull(message = "자동저축 월 상한액은 필수입니다.")
  @Positive(message = "자동저축 월 상한액은 0보다 커야 합니다.")
  private Long maxAmount;

}
