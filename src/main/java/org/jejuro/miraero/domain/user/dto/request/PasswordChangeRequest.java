package org.jejuro.miraero.domain.user.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeRequest {

  @NotBlank(message = "Current password is required.")
  private String currentPassword;

  @NotBlank(message = "New password is required.")
  @Size(min = 8, message = "New password must be at least 8 characters.")
  private String newPassword;

  @NotBlank(message = "New password confirmation is required.")
  private String newPasswordConfirm;
}
