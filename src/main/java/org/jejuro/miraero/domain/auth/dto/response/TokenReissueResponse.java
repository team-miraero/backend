package org.jejuro.miraero.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenReissueResponse {

  private TokenResponse token;
  private LoginUserResponse user;
}
