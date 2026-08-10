package org.jejuro.miraero.domain.account.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.account.dto.response.AccountListResponse;
import org.jejuro.miraero.domain.account.dto.response.AccountResponse;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

  private final AccountMapper accountMapper;

  @Override
  public AccountListResponse getAccounts(Long userId) {
    return AccountListResponse.builder()
        .accounts(accountMapper.findAllByUserId(userId))
        .build();
  }

  @Override
  public AccountResponse findAccount(Long accountId) {
    return accountMapper.findResponseById(accountId);
  }
}
