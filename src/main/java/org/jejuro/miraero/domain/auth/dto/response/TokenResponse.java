package org.jejuro.miraero.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {

  private String accessToken;
  private String refreshToken;
  private String tokenType;
  private Long accessTokenExpiresIn;
  private Long refreshTokenExpiresIn;

}
