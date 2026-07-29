package org.jejuro.miraero.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jejuro.miraero.domain.user.dto.request.UserSignUpRequest;
import org.jejuro.miraero.domain.user.dto.response.UserSignUpResponse;
import org.jejuro.miraero.domain.user.exception.UserErrorCode;
import org.jejuro.miraero.domain.user.service.UserService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  private MockMvc mockMvc;

  private ObjectMapper objectMapper;

  @Mock
  private UserService userService;

  @BeforeEach
  void setUp() {
    UserController userController =
        new UserController(userService);

    LocalValidatorFactoryBean validator =
        new LocalValidatorFactoryBean();

    validator.afterPropertiesSet();

    mockMvc = MockMvcBuilders
        .standaloneSetup(userController)
        .setControllerAdvice(new GlobalExceptionHandler())
        .setValidator(validator)
        .build();

    objectMapper = new ObjectMapper();
  }

  @Test
  @DisplayName("회원가입에 성공하면 201 상태 코드와 회원 정보를 반환한다")
  void signUp_success() throws Exception {
    // given
    UserSignUpRequest request =
        new UserSignUpRequest(
            "test@example.com",
            "password123!"
        );

    UserSignUpResponse response =
        new UserSignUpResponse(
            1L,
            "테스트 사용자",
            "test@example.com"
        );

    given(userService.signUp(any(UserSignUpRequest.class)))
        .willReturn(response);

    // when & then
    mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(request)
                )
        )
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.userId").value(1))
        .andExpect(
            jsonPath("$.data.name")
                .value("테스트 사용자")
        )
        .andExpect(
            jsonPath("$.data.email")
                .value("test@example.com")
        );

    verify(userService)
        .signUp(any(UserSignUpRequest.class));
  }

  @Test
  @DisplayName("이메일 형식이 올바르지 않으면 400 상태 코드를 반환한다")
  void signUp_invalidEmail() throws Exception {
    // given
    UserSignUpRequest request =
        new UserSignUpRequest(
            "invalid-email",
            "password123!"
        );

    // when & then
    mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(request)
                )
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.error.code")
                .value("COMMON_002")
        );

    verify(userService, never())
        .signUp(any(UserSignUpRequest.class));
  }

  @Test
  @DisplayName("이메일이 비어 있으면 400 상태 코드를 반환한다")
  void signUp_blankEmail() throws Exception {
    // given
    UserSignUpRequest request =
        new UserSignUpRequest(
            "",
            "password123!"
        );

    // when & then
    mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(request)
                )
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.error.code")
                .value("COMMON_002")
        );

    verify(userService, never())
        .signUp(any(UserSignUpRequest.class));
  }

  @Test
  @DisplayName("이미 가입된 이메일이면 409 상태 코드와 USER_001을 반환한다")
  void signUp_duplicateEmail() throws Exception {
    // given
    UserSignUpRequest request =
        new UserSignUpRequest(
            "test@example.com",
            "password123!"
        );

    given(userService.signUp(any(UserSignUpRequest.class)))
        .willThrow(
            new BusinessException(
                UserErrorCode.EMAIL_ALREADY_EXISTS
            )
        );

    // when & then
    mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(request)
                )
        )
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.error.code")
                .value("USER_001")
        )
        .andExpect(
            jsonPath("$.error.message")
                .value("이미 가입된 이메일입니다.")
        );

    verify(userService)
        .signUp(any(UserSignUpRequest.class));
  }
}
