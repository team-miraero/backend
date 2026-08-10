package org.jejuro.miraero.domain.autotransfer.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jejuro.miraero.domain.account.domain.Account;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.domain.autotransfer.domain.AutoTransfer;
import org.jejuro.miraero.domain.autotransfer.dto.request.AutoTransferCreateRequest;
import org.jejuro.miraero.domain.autotransfer.mapper.AutoTransferMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AutoTransferServiceImplTest {

  private static final Long USER_ID = 1L;
  private static final Long WITHDRAWAL_ACCOUNT_ID = 10L;
  private static final Long MONEY_BOX_ID = 20L;

  @Mock
  private AutoTransferMapper autoTransferMapper;

  @Mock
  private AccountMapper accountMapper;

  @InjectMocks
  private AutoTransferServiceImpl autoTransferService;

  @Test
  @DisplayName("출금계좌가 내 소유면 자동이체 설정을 저장한다")
  void createMoneyBoxAutoTransfer_success() {
    when(accountMapper.findByIdAndUserId(WITHDRAWAL_ACCOUNT_ID, USER_ID))
        .thenReturn(Account.of(
            USER_ID, 1L, 999L, "CHECKING", "KB 입출금통장",
            new byte[]{1}, "hash", "123*****90", 100_000L, "ACTIVE",
            null, null, null, null
        ));

    autoTransferService.createMoneyBoxAutoTransfer(
        USER_ID, MONEY_BOX_ID, "999*****90", createRequest()
    );

    ArgumentCaptor<AutoTransfer> captor = ArgumentCaptor.forClass(AutoTransfer.class);
    verify(autoTransferMapper).save(captor.capture());
    org.junit.jupiter.api.Assertions.assertEquals(
        WITHDRAWAL_ACCOUNT_ID, captor.getValue().getWithdrawalAccountId());
    org.junit.jupiter.api.Assertions.assertEquals(MONEY_BOX_ID, captor.getValue().getMoneyBoxId());
  }

  @Test
  @DisplayName("출금계좌가 내 소유가 아니면 예외를 던지고 저장하지 않는다")
  void createMoneyBoxAutoTransfer_notOwned_fail() {
    when(accountMapper.findByIdAndUserId(WITHDRAWAL_ACCOUNT_ID, USER_ID))
        .thenReturn(null);

    assertThrows(BusinessException.class, () ->
        autoTransferService.createMoneyBoxAutoTransfer(
            USER_ID, MONEY_BOX_ID, "999*****90", createRequest()
        ));

    verify(autoTransferMapper, never()).save(any());
  }

  private AutoTransferCreateRequest createRequest() {
    AutoTransferCreateRequest request = new AutoTransferCreateRequest();
    ReflectionTestUtils.setField(request, "withdrawalAccountId", WITHDRAWAL_ACCOUNT_ID);
    ReflectionTestUtils.setField(request, "amount", 300_000L);
    ReflectionTestUtils.setField(request, "transferDay", 5);
    return request;
  }
}
