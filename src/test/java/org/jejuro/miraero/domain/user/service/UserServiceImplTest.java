package org.jejuro.miraero.domain.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jejuro.miraero.domain.mydata.service.MyDataConsentService;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.dto.response.ProfileResponse;
import org.jejuro.miraero.domain.user.exception.UserErrorCode;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock
  private UserMapper userMapper;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private MyDataConsentService myDataConsentService;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserServiceImpl(
        userMapper,
        passwordEncoder,
        myDataConsentService
    );
  }

  @Test
  @DisplayName("회원 생성 요청이 유효하면 회원을 저장하고 동의 현황을 생성한다")
  void create_success() {
    UserCreateCommand command =
        new UserCreateCommand("test@example.com", "password123!");

    when(userMapper.existsByEmail(command.getEmail()))
        .thenReturn(false);
    when(passwordEncoder.encode(command.getPassword()))
        .thenReturn("encodedPassword");
    when(userMapper.save(any(User.class)))
        .thenReturn(1);

    User user = userService.create(command);

    assertNotNull(user.getName());
    assertEquals("test@example.com", user.getEmail());
    assertEquals("encodedPassword", user.getPasswordHash());

    verify(userMapper).existsByEmail("test@example.com");
    verify(passwordEncoder).encode("password123!");
    verify(userMapper).save(any(User.class));
    verify(myDataConsentService).createInitialConsent(user.getUserId());
  }

  @Test
  @DisplayName("이미 가입된 이메일이면 USER_001 예외를 던진다")
  void create_duplicateEmail() {
    UserCreateCommand command =
        new UserCreateCommand("test@example.com", "password123!");

    when(userMapper.existsByEmail(command.getEmail()))
        .thenReturn(true);

    BusinessException exception = assertThrows(
        BusinessException.class,
        () -> userService.create(command)
    );

    assertEquals(UserErrorCode.EMAIL_ALREADY_EXISTS, exception.getErrorCode());

    verify(userMapper).existsByEmail("test@example.com");
  }

  @Test
  @DisplayName("프로필 조회에 성공하면 사용자 프로필을 반환한다")
  void getProfile_success() {
    Long userId = 1L;
    ProfileResponse response = ProfileResponse.builder()
        .userId(userId)
        .name("김미래")
        .email("miraero@gmail.com")
        .birthDate("2001-03-15")
        .profileImageUrl(null)
        .company("KB금융그룹")
        .monthlyIncome(2_850_000L)
        .kbpayLinked(true)
        .build();
    when(userMapper.findProfileById(userId)).thenReturn(response);

    ProfileResponse result = userService.getProfile(userId);

    assertEquals(userId, result.getUserId());
    assertEquals("김미래", result.getName());
    assertEquals("miraero@gmail.com", result.getEmail());
    assertEquals("2001-03-15", result.getBirthDate());
    assertEquals("KB금융그룹", result.getCompany());
    assertEquals(2_850_000L, result.getMonthlyIncome());
    assertEquals(true, result.isKbpayLinked());
    verify(userMapper).findProfileById(userId);
  }

  @Test
  @DisplayName("프로필 조회 대상 사용자가 없으면 리소스 없음 예외를 발생시킨다")
  void getProfile_notFound() {
    Long userId = 99L;
    when(userMapper.findProfileById(userId)).thenReturn(null);

    BusinessException exception = assertThrows(
        BusinessException.class,
        () -> userService.getProfile(userId)
    );

    assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
    verify(userMapper).findProfileById(userId);
  }
}
