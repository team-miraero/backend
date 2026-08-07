package org.jejuro.miraero.domain.mydata.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyDataConnectionListResponse {

  private List<MyDataConnectionResponse> connections;
}
