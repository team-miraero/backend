package org.jejuro.miraero.domain.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jejuro.miraero.domain.user.dto.request.UserSignUpRequest;
import org.jejuro.miraero.domain.user.dto.response.UserSignUpResponse;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.domain.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

  @Mock
  private UserMapper userMapper;

  @Mock
  private PasswordEncoder passwordEncoder;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserServiceImpl(userMapper, passwordEncoder);
  }

    @Test
    @DisplayName("회원가입 요청이 유효하면 회원을 저장하고 응답을 반환한다")
    void signUp() {
      // given
      UserSignUpRequest request =
          new UserSignUpRequest("test@example.com", "password123!");

      when(userMapper.existsByEmail(request.getEmail()))
          .thenReturn(false);

      when(passwordEncoder.encode(request.getPassword()))
          .thenReturn("encodedPassword");

      when(userMapper.save(any(User.class)))
          .thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return 1;
          });

      // when
      UserSignUpResponse response = userService.signUp(request);

      // then
      assertEquals("테스트 사용자", response.getName());
      assertEquals("test@example.com", response.getEmail());

      verify(userMapper).existsByEmail("test@example.com");
      verify(passwordEncoder).encode("password123!");
      verify(userMapper).save(any(User.class));
    }
}
