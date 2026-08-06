package org.jejuro.miraero.domain.pacemaker.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;

@Getter
@Setter
public class PaceMakerHistorySearchCondition {

  private Integer page = 0;
  private Integer size = 10;

  public void validate() {
    if (page == null) {
      page = 0;
    }

    if (size == null) {
      size = 10;
    }

    if (page < 0 || size <= 0) {
      throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
    }
  }

  public long getOffset() {
    return (long) page * size;
  }
}
