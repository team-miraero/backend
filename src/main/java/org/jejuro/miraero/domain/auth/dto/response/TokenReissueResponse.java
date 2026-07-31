package org.jejuro.miraero.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

@Getter
public class TokenReissueResponse {

  private final TokenResponse token;
  private final LoginUserResponse user;

  @JsonIgnore
  private final String refreshToken;

  @JsonIgnore
  private final Long refreshTokenExpiresIn;

  public TokenReissueResponse(
      String accessToken,
      String refreshToken,
      Long accessTokenExpiresIn,
      Long refreshTokenExpiresIn,
      LoginUserResponse user
  ) {
    this.token = new TokenResponse(
        accessToken,
        "Bearer",
        accessTokenExpiresIn
    );
    this.user = user;
    this.refreshToken = refreshToken;
    this.refreshTokenExpiresIn = refreshTokenExpiresIn;
  }
}
