package org.jejuro.miraero.domain.account.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.jejuro.miraero.domain.account.domain.Account;
import org.jejuro.miraero.domain.account.dto.response.AccountResponse;
import org.jejuro.miraero.global.config.RootConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

@SpringJUnitConfig
@ContextConfiguration(classes = RootConfig.class)
@Transactional
@Rollback
class AccountMapperTest {

  private static final Long EX_ACCOUNT_ID = 999201L;

  @Autowired
  private AccountMapper accountMapper;

  @Test
  @DisplayName("같은 ex_account_id로 두 번 upsert해도 한 건만 남는다")
  void upsert_isIdempotent() {
    accountMapper.upsert(createAccount(3400000L));
    Long firstId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);
    assertNotNull(firstId);

    accountMapper.upsert(createAccount(5000000L));
    Long secondId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);

    assertEquals(firstId, secondId);
  }

  @Test
  @DisplayName("소유자가 맞으면 계좌를 조회하고, 다른 사용자면 null을 반환한다")
  void findByIdAndUserId_ownershipCheck() {
    accountMapper.upsert(createAccount(3400000L));
    Long accountId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);

    assertNotNull(accountMapper.findByIdAndUserId(accountId, 1L));
    assertNull(accountMapper.findByIdAndUserId(accountId, 999L));
  }

  @Test
  @DisplayName("소유자가 맞으면 true, 다른 사용자면 false를 반환한다")
  void existsByIdAndUserId_ownershipCheck() {
    accountMapper.upsert(createAccount(3400000L));
    Long accountId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);

    assertTrue(accountMapper.existsByIdAndUserId(accountId, 1L));
    assertFalse(accountMapper.existsByIdAndUserId(accountId, 999L));
  }

  @Test
  @DisplayName("사용자의 전체 계좌 목록을 은행명과 함께 조회한다")
  void findAllByUserId_includesInstitutionName() {
    accountMapper.upsert(createAccount(3400000L));

    java.util.List<AccountResponse> accounts = accountMapper.findAllByUserId(1L, null);

    assertTrue(accounts.stream().anyMatch(a ->
        "KB 입출금통장".equals(a.getAccountName()) && a.getInstitutionName() != null));
  }

  @Test
  @DisplayName("accountType으로 필터링하면 다른 유형 계좌는 제외된다")
  void findAllByUserId_filtersByAccountType() {
    accountMapper.upsert(createAccount(3400000L));

    java.util.List<AccountResponse> checking = accountMapper.findAllByUserId(1L, "CHECKING");
    java.util.List<AccountResponse> savings = accountMapper.findAllByUserId(1L, "SAVINGS");

    assertTrue(checking.stream().anyMatch(a -> "KB 입출금통장".equals(a.getAccountName())));
    assertTrue(savings.stream().noneMatch(a -> "KB 입출금통장".equals(a.getAccountName())));
  }

  @Test
  @DisplayName("소유자가 맞으면 은행명 포함 상세를 조회하고, 다른 사용자면 null을 반환한다")
  void findResponseByIdAndUserId_ownershipCheck() {
    accountMapper.upsert(createAccount(3400000L));
    Long accountId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);

    AccountResponse own = accountMapper.findResponseByIdAndUserId(accountId, 1L);
    AccountResponse other = accountMapper.findResponseByIdAndUserId(accountId, 999L);

    assertNotNull(own);
    assertEquals(3400000L, own.getBalance());
    assertNull(other);
  }

  @Test
  @DisplayName("계좌 ID로 은행명을 포함한 상세정보를 조회한다")
  void findResponseById_returnsDetail() {
    accountMapper.upsert(createAccount(3400000L));
    Long accountId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);

    AccountResponse response = accountMapper.findResponseById(accountId);

    assertNotNull(response);
    assertEquals(3400000L, response.getBalance());
    assertNotNull(response.getInstitutionName());
  }

  private Account createAccount(Long balance) {
    return Account.of(
        1L,
        1L,
        EX_ACCOUNT_ID,
        "CHECKING",
        "KB 입출금통장",
        new byte[]{1, 2, 3},
        "hash-999201",
        "1234*****90",
        balance,
        "ACTIVE",
        LocalDate.of(2020, 1, 1),
        null,
        new BigDecimal("0.1000"),
        null
    );
  }
}
