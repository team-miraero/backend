package org.jejuro.miraero.domain.mydata.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.domain.mydata.client.MyDataApiClient;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataAccountResponse;
import org.jejuro.miraero.domain.mydata.mapper.ReferenceDataMapper;
import org.jejuro.miraero.domain.mydata.repository.MyDataTokenRepository;
import org.jejuro.miraero.domain.transaction.mapper.TransactionSyncMapper;
import org.jejuro.miraero.global.crypto.AccountNumberCrypto;
import org.jejuro.miraero.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MyDataSyncServiceImplTest {

  private static final Long USER_ID = 1L;
  private static final Long KB_USER_ID = 10001L;

  @Mock
  private MyDataApiClient myDataApiClient;
  @Mock
  private MyDataTokenRepository myDataTokenRepository;
  @Mock
  private ReferenceDataMapper referenceDataMapper;
  @Mock
  private AccountMapper accountMapper;
  @Mock
  private TransactionSyncMapper transactionSyncMapper;

  private MyDataSyncService myDataSyncService;

  @BeforeEach
  void setUp() {
    myDataSyncService = new MyDataSyncServiceImpl(
        myDataApiClient,
        myDataTokenRepository,
        referenceDataMapper,
        accountMapper,
        transactionSyncMapper,
        new AccountNumberCrypto()
    );
  }

  @Test
  @DisplayName("토큰이 없으면 예외를 던지고 외부 호출을 하지 않는다")
  void sync_tokenMissing() {
    when(myDataTokenRepository.findByUserId(USER_ID)).thenReturn(null);

    assertThrows(BusinessException.class, () -> myDataSyncService.sync(USER_ID, KB_USER_ID));

    verify(myDataApiClient, never()).getAccounts(any(), any());
  }

  @Test
  @DisplayName("등록되지 않은 금융기관 코드면 예외를 던진다")
  void sync_unknownInstitution() {
    when(myDataTokenRepository.findByUserId(USER_ID)).thenReturn("token-1");
    when(myDataApiClient.getAccounts(KB_USER_ID, "token-1"))
        .thenReturn(List.of(new MyDataAccountResponse()));
    when(referenceDataMapper.findFinancialInstitutionIdByCode(null)).thenReturn(null);

    assertThrows(BusinessException.class, () -> myDataSyncService.sync(USER_ID, KB_USER_ID));

    verify(transactionSyncMapper, never()).upsert(any());
  }
}
