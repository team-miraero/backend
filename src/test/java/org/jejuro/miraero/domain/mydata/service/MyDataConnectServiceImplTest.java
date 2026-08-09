package org.jejuro.miraero.domain.mydata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jejuro.miraero.domain.mydata.client.MyDataAuthClient;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataTokenResponse;
import org.jejuro.miraero.domain.mydata.dto.response.MyDataConnectResponse;
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

  @Mock
  private MyDataAuthClient myDataAuthClient;
  @Mock
  private MyDataTokenRepository myDataTokenRepository;
  @Mock
  private MyDataSyncService myDataSyncService;
  @Mock
  private UserMapper userMapper;

  private MyDataConnectService myDataConnectService;

  @BeforeEach
  void setUp() {
    myDataConnectService = new MyDataConnectServiceImpl(
        myDataAuthClient, myDataTokenRepository, myDataSyncService, userMapper);
  }

  @Test
  @DisplayName("연동에 성공하면 토큰을 저장하고 kbUserId를 반환한다")
  void connect() {
    User user = createUser("miraero01@test.com");
    when(userMapper.findById(USER_ID)).thenReturn(user);
    when(myDataAuthClient.requestAuthorizationCode("miraero01@test.com")).thenReturn("code-1");

    MyDataTokenResponse token = new MyDataTokenResponse();
    ReflectionTestUtils.setField(token, "accessToken", "token-1");
    ReflectionTestUtils.setField(token, "expiresIn", 3600L);
    ReflectionTestUtils.setField(token, "kbUserId", 10001L);
    when(myDataAuthClient.exchangeToken("code-1")).thenReturn(token);

    MyDataConnectResponse response = myDataConnectService.connect(USER_ID);

    assertEquals(10001L, response.getKbUserId());
    verify(myDataTokenRepository).save(USER_ID, "token-1", 3600L);
  }

  @Test
  @DisplayName("존재하지 않는 사용자면 예외를 던진다")
  void connect_userNotFound() {
    when(userMapper.findById(USER_ID)).thenReturn(null);

    assertThrows(BusinessException.class, () -> myDataConnectService.connect(USER_ID));
  }

  private User createUser(String email) {
    return User.create(null, null, null, null, email, "hash", null);
  }
}
