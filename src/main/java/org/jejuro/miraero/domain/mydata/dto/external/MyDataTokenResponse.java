package org.jejuro.miraero.domain.mydata.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MyDataTokenResponse {

  private String accessToken;
  private Long expiresIn;
  private Long kbUserId;
}
