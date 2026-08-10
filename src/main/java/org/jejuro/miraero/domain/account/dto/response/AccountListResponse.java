package org.jejuro.miraero.domain.account.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountListResponse {

  private Long totalBalance;
  private List<AccountResponse> accounts;
}
