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
  private Boolean mydataConnected;
  private Boolean goalSet;

  public static LoginUserResponse from(User user, boolean goalSet) {
    return new LoginUserResponse(
        user.getUserId(),
        user.getName(),
        user.getEmail(),
        user.getKbPayId() != null,
        goalSet
    );
  }
}
