package org.jejuro.miraero.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSignUpResponse {

  private Long userId;
  private String name;
  private String email;

}
