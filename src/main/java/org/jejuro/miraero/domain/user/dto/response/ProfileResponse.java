package org.jejuro.miraero.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {

  private Long userId;
  private String name;
  private String email;
  private String birthDate;
  private String profileImageUrl;
  private String company;
  private Long monthlyIncome;
  private boolean kbpayLinked;
}
