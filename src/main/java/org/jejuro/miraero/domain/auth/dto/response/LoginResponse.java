package org.jejuro.miraero.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

@Getter
public class LoginResponse {

  private final TokenResponse token;
  private final Boolean autoLogin;
  private final LoginUserResponse user;

  @JsonIgnore
  private final String refreshToken;

  @JsonIgnore
  private final Long refreshTokenExpiresIn;

  public LoginResponse(
      String accessToken,
      String refreshToken,
      Long accessTokenExpiresIn,
      Long refreshTokenExpiresIn,
      Boolean autoLogin,
      LoginUserResponse user
  ) {
    this.token = new TokenResponse(
        accessToken,
        "Bearer",
        accessTokenExpiresIn
    );
    this.autoLogin = autoLogin;
    this.user = user;
    this.refreshToken = refreshToken;
    this.refreshTokenExpiresIn = refreshTokenExpiresIn;
  }
}
