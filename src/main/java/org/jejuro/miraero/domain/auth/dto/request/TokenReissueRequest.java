package org.jejuro.miraero.domain.auth.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenReissueRequest {

  @NotBlank(message = "Refresh Token은 필수입니다.")
  private String refreshToken;
}
