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
import org.jejuro.miraero.domain.moneybox.domain.MoneyBox;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBoxType;
import org.jejuro.miraero.domain.moneybox.mapper.MoneyBoxMapper;
import org.jejuro.miraero.domain.mydata.mapper.ReferenceDataMapper;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.global.config.RootConfig;
import org.junit.jupiter.api.BeforeEach;
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
  private static final String KB_CODE = "004";

  @Autowired
  private AccountMapper accountMapper;

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private ReferenceDataMapper referenceDataMapper;

  @Autowired
  private MoneyBoxMapper moneyBoxMapper;

  private Long userId;
  private Long financialInstitutionId;

  // DB에 미리 있는 사용자에 기대면 초기화된 환경에서 FK 위반으로 깨진다. @Rollback이라 매 테스트 후 사라진다.
  @BeforeEach
  void setUp() {
    User user = User.create(
        "계좌테스트", LocalDate.of(2000, 1, 1), "테스트회사", 3_000_000L,
        "account-mapper-test@test.com", "hash", null
    );
    userMapper.save(user);
    this.userId = user.getUserId();
    this.financialInstitutionId = referenceDataMapper.findFinancialInstitutionIdByCode(KB_CODE);
  }

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

    assertNotNull(accountMapper.findByIdAndUserId(accountId, userId));
    assertNull(accountMapper.findByIdAndUserId(accountId, userId + 999L));
  }

  @Test
  @DisplayName("소유자가 맞으면 true, 다른 사용자면 false를 반환한다")
  void existsByIdAndUserId_ownershipCheck() {
    accountMapper.upsert(createAccount(3400000L));
    Long accountId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);

    assertTrue(accountMapper.existsByIdAndUserId(accountId, userId));
    assertFalse(accountMapper.existsByIdAndUserId(accountId, userId + 999L));
  }

  @Test
  @DisplayName("사용자의 전체 계좌 목록을 은행명과 함께 조회한다")
  void findAllByUserId_includesInstitutionName() {
    accountMapper.upsert(createAccount(3400000L));

    java.util.List<AccountResponse> accounts = accountMapper.findAllByUserId(userId, null);

    assertTrue(accounts.stream().anyMatch(a ->
        "KB 입출금통장".equals(a.getAccountName()) && a.getInstitutionName() != null));
  }

  @Test
  @DisplayName("accountType으로 필터링하면 다른 유형 계좌는 제외된다")
  void findAllByUserId_filtersByAccountType() {
    accountMapper.upsert(createAccount(3400000L));

    java.util.List<AccountResponse> checking = accountMapper.findAllByUserId(userId, "CHECKING");
    java.util.List<AccountResponse> savings = accountMapper.findAllByUserId(userId, "SAVINGS");

    assertTrue(checking.stream().anyMatch(a -> "KB 입출금통장".equals(a.getAccountName())));
    assertTrue(savings.stream().noneMatch(a -> "KB 입출금통장".equals(a.getAccountName())));
  }

  @Test
  @DisplayName("소유자가 맞으면 은행명 포함 상세를 조회하고, 다른 사용자면 null을 반환한다")
  void findResponseByIdAndUserId_ownershipCheck() {
    accountMapper.upsert(createAccount(3400000L));
    Long accountId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);

    AccountResponse own = accountMapper.findResponseByIdAndUserId(accountId, userId);
    AccountResponse other = accountMapper.findResponseByIdAndUserId(accountId, userId + 999L);

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

  @Test
  @DisplayName("저금통이 달린 계좌는 조회 잔액에서 저금통 금액이 빠진다")
  void findResponseById_excludesMoneyBoxBalance() {
    accountMapper.upsert(createAccount(3_400_000L));
    Long accountId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);

    moneyBoxMapper.insert(MoneyBox.builder()
        .userId(userId)
        .accountId(accountId)
        .balance(1_000_000L)
        .moneyBoxType(MoneyBoxType.GOAL)
        .build());

    assertEquals(2_400_000L, accountMapper.findResponseById(accountId).getBalance());
    assertEquals(2_400_000L,
        accountMapper.findResponseByIdAndUserId(accountId, userId).getBalance());
  }

  @Test
  @DisplayName("한 계좌에 저금통이 여러 개면 합계만큼 빠진다")
  void findAllByUserId_excludesAllMoneyBoxBalances() {
    accountMapper.upsert(createAccount(3_400_000L));
    Long accountId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);

    moneyBoxMapper.insert(MoneyBox.builder()
        .userId(userId).accountId(accountId).balance(1_000_000L)
        .moneyBoxType(MoneyBoxType.GOAL).build());
    moneyBoxMapper.insert(MoneyBox.builder()
        .userId(userId).accountId(accountId).balance(500_000L)
        .moneyBoxType(MoneyBoxType.SAVING).build());

    AccountResponse account = accountMapper.findAllByUserId(userId, null).stream()
        .filter(a -> accountId.equals(a.getAccountId()))
        .findFirst()
        .orElseThrow();

    assertEquals(1_900_000L, account.getBalance());
  }

  private Account createAccount(Long balance) {
    return Account.of(
        userId,
        financialInstitutionId,
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
