package org.jejuro.miraero.domain.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.util.List;
import org.jejuro.miraero.domain.account.dto.response.AccountListResponse;
import org.jejuro.miraero.domain.account.dto.response.AccountResponse;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

  private static final Long USER_ID = 1L;

  @Mock
  private AccountMapper accountMapper;

  @InjectMocks
  private AccountServiceImpl accountService;

  @Test
  @DisplayName("사용자의 전체 계좌 목록을 반환한다")
  void getAccounts_returnsAllAccounts() {
    List<AccountResponse> accounts = List.of(
        AccountResponse.builder().accountId(1L).accountType("CHECKING").build(),
        AccountResponse.builder().accountId(2L).accountType("SAVINGS").build()
    );
    when(accountMapper.findAllByUserId(USER_ID)).thenReturn(accounts);

    AccountListResponse response = accountService.getAccounts(USER_ID);

    assertEquals(2, response.getAccounts().size());
  }

  @Test
  @DisplayName("계좌 ID로 상세정보를 조회한다")
  void findAccount_returnsDetail() {
    AccountResponse account = AccountResponse.builder().accountId(1L).balance(500_000L).build();
    when(accountMapper.findResponseById(1L)).thenReturn(account);

    AccountResponse response = accountService.findAccount(1L);

    assertEquals(500_000L, response.getBalance());
  }

  @Test
  @DisplayName("존재하지 않는 계좌면 null을 반환한다")
  void findAccount_notFound_returnsNull() {
    when(accountMapper.findResponseById(999L)).thenReturn(null);

    assertNull(accountService.findAccount(999L));
  }
}
