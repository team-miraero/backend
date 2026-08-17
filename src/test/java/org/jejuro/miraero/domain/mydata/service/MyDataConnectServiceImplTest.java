package org.jejuro.miraero.domain.mydata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.jejuro.miraero.domain.mydata.client.MyDataAuthClient;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataTokenResponse;
import org.jejuro.miraero.domain.mydata.dto.response.MyDataConnectResponse;
import org.jejuro.miraero.domain.mydata.exception.MyDataErrorCode;
import org.jejuro.miraero.domain.mydata.mapper.MyDataConsentMapper;
import org.jejuro.miraero.domain.mydata.mapper.ReferenceDataMapper;
import org.jejuro.miraero.domain.mydata.repository.MyDataTokenRepository;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MyDataConnectServiceImplTest {

  private static final Long USER_ID = 1L;
  private static final Long INSTITUTION_ID = 1L;

  @Mock
  private MyDataAuthClient myDataAuthClient;
  @Mock
  private MyDataTokenRepository myDataTokenRepository;
  @Mock
  private MyDataSyncService myDataSyncService;
  @Mock
  private UserMapper userMapper;
  @Mock
  private ReferenceDataMapper referenceDataMapper;
  @Mock
  private MyDataConsentMapper myDataConsentMapper;

  private MyDataConnectService myDataConnectService;

  @BeforeEach
  void setUp() {
    myDataConnectService = new MyDataConnectServiceImpl(
        myDataAuthClient, myDataTokenRepository, myDataSyncService, userMapper,
        referenceDataMapper, myDataConsentMapper);
  }

  @Test
  @DisplayName("연동에 성공하면 토큰을 저장하고 kbUserId를 반환하며 연동 상태를 기록한다")
  void connect() {
    User user = createUser("miraero01@test.com");
    when(userMapper.findById(USER_ID)).thenReturn(user);
    when(myDataAuthClient.requestAuthorizationCode("miraero01@test.com")).thenReturn("code-1");
    when(referenceDataMapper.findFinancialInstitutionIdByCode("004")).thenReturn(INSTITUTION_ID);

    MyDataTokenResponse token = createToken("token-1", 3600L, 10001L);
    when(myDataAuthClient.exchangeToken("code-1")).thenReturn(token);

    MyDataConnectResponse response = myDataConnectService.connect(USER_ID);

    assertEquals(10001L, response.getKbUserId());
    verify(myDataTokenRepository).save(USER_ID, "token-1", 3600L);
    verify(myDataConsentMapper).upsertConnection(eq(USER_ID), eq(INSTITUTION_ID), eq("CONNECTED"), any());
  }

  @Test
  @DisplayName("연동 시 목서버가 내려준 본인확인 정보로 회원가입 목업 값을 덮어쓴다")
  void connect_updatesProfileFromMyData() {
    User user = createUser("miraero01@test.com");
    when(userMapper.findById(USER_ID)).thenReturn(user);
    when(myDataAuthClient.requestAuthorizationCode("miraero01@test.com")).thenReturn("code-1");
    when(referenceDataMapper.findFinancialInstitutionIdByCode("004")).thenReturn(INSTITUTION_ID);

    MyDataTokenResponse token = createToken("token-1", 3600L, 10001L);
    ReflectionTestUtils.setField(token, "name", "탁민주");
    ReflectionTestUtils.setField(token, "birthDate", LocalDate.of(1999, 4, 18));
    ReflectionTestUtils.setField(token, "monthlyIncome", 2_850_000L);
    ReflectionTestUtils.setField(token, "companyName", "중견기업J");
    when(myDataAuthClient.exchangeToken("code-1")).thenReturn(token);

    myDataConnectService.connect(USER_ID);

    verify(userMapper).updateProfile(
        USER_ID, "탁민주", LocalDate.of(1999, 4, 18), "중견기업J", 2_850_000L);
  }

  @Test
  @DisplayName("존재하지 않는 사용자면 예외를 던진다")
  void connect_userNotFound() {
    when(userMapper.findById(USER_ID)).thenReturn(null);

    assertThrows(BusinessException.class, () -> myDataConnectService.connect(USER_ID));
  }

  @Test
  @DisplayName("동기화 성공 시 synced_at을 갱신한다")
  void sync_updatesSyncedAt() {
    User user = createUser("miraero01@test.com");
    ReflectionTestUtils.setField(user, "kbPayId", 10001L);
    when(userMapper.findById(USER_ID)).thenReturn(user);
    when(referenceDataMapper.findFinancialInstitutionIdByCode("004")).thenReturn(INSTITUTION_ID);

    myDataConnectService.sync(USER_ID);

    verify(myDataSyncService).sync(USER_ID, 10001L);
    verify(myDataConsentMapper).updateSyncedAt(USER_ID, INSTITUTION_ID);
  }

  @Test
  @DisplayName("토큰이 만료됐으면 재인증 후 한 번만 재시도한다")
  void sync_refreshesExpiredToken() {
    User user = createUser("miraero01@test.com");
    ReflectionTestUtils.setField(user, "kbPayId", 10001L);
    when(userMapper.findById(USER_ID)).thenReturn(user);
    when(referenceDataMapper.findFinancialInstitutionIdByCode("004")).thenReturn(INSTITUTION_ID);
    when(myDataAuthClient.requestAuthorizationCode("miraero01@test.com")).thenReturn("code-1");
    when(myDataAuthClient.exchangeToken("code-1"))
        .thenReturn(createToken("token-2", 3600L, 10001L));

    doThrow(new BusinessException(MyDataErrorCode.MYDATA_TOKEN_EXPIRED))
        .doNothing()
        .when(myDataSyncService).sync(anyLong(), anyLong());

    myDataConnectService.sync(USER_ID);

    verify(myDataSyncService, times(2)).sync(anyLong(), anyLong());
    verify(myDataTokenRepository).save(USER_ID, "token-2", 3600L);
  }

  private User createUser(String email) {
    return User.create(null, null, null, null, email, "hash", null);
  }

  private MyDataTokenResponse createToken(String accessToken, Long expiresIn, Long kbUserId) {
    MyDataTokenResponse token = new MyDataTokenResponse();
    ReflectionTestUtils.setField(token, "accessToken", accessToken);
    ReflectionTestUtils.setField(token, "expiresIn", expiresIn);
    ReflectionTestUtils.setField(token, "kbUserId", kbUserId);
    return token;
  }
}
