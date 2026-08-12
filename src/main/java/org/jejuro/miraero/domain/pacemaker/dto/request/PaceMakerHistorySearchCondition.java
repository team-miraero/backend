package org.jejuro.miraero.domain.pacemaker.dto.request;

import lombok.Getter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Setter;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;

@Getter
@Setter
@ApiModel(description = "페이스메이커 이력 조회 조건")
public class PaceMakerHistorySearchCondition {

  @ApiModelProperty(value = "페이지 번호. 0부터 시작", example = "0")
  private Integer page = 0;
  @ApiModelProperty(value = "페이지당 항목 수", example = "10")
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
