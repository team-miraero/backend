package org.jejuro.miraero.domain.account.service;

import org.jejuro.miraero.domain.account.dto.response.AccountListResponse;
import org.jejuro.miraero.domain.account.dto.response.AccountResponse;

public interface AccountService {

  AccountListResponse getAccounts(Long userId);

  AccountResponse findAccount(Long accountId);
}
