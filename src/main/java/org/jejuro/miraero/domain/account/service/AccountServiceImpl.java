package org.jejuro.miraero.domain.account.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.account.dto.response.AccountListResponse;
import org.jejuro.miraero.domain.account.dto.response.AccountResponse;
import org.jejuro.miraero.domain.account.exception.AccountErrorCode;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

  private final AccountMapper accountMapper;

  @Override
  public AccountListResponse getAccounts(Long userId, String accountType) {
    List<AccountResponse> accounts = accountMapper.findAllByUserId(userId, accountType);
    long totalBalance = accounts.stream()
        .mapToLong(AccountResponse::getBalance)
        .sum();

    return AccountListResponse.builder()
        .totalBalance(totalBalance)
        .accounts(accounts)
        .build();
  }

  @Override
  public AccountResponse findAccount(Long accountId) {
    return accountMapper.findResponseById(accountId);
  }

  @Override
  public AccountResponse getAccount(Long accountId, Long userId) {
    AccountResponse account = accountMapper.findResponseByIdAndUserId(accountId, userId);
    if (account == null) {
      throw new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND);
    }
    return account;
  }
}
