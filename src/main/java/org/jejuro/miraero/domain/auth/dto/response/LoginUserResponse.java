package org.jejuro.miraero.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jejuro.miraero.domain.user.domain.User;

@Getter
@AllArgsConstructor
public class LoginUserResponse {

  private Long userId;
  private String name;
  private String email;

  public static LoginUserResponse from(User user) {
    return new LoginUserResponse(
        user.getUserId(),
        user.getName(),
        user.getEmail()
    );
  }
}
