package org.jejuro.miraero.domain.account.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountListResponse {

  private List<AccountResponse> accounts;
}
