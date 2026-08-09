package org.jejuro.miraero.domain.account.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.jejuro.miraero.domain.account.domain.Account;
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
