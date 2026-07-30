package org.jejuro.miraero.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

  private TokenResponse token;
  private Boolean autoLogin;
  private LoginUserResponse user;
}
