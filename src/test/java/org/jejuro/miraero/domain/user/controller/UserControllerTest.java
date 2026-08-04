package org.jejuro.miraero.domain.user.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.jejuro.miraero.domain.user.dto.response.ProfileResponse;
import org.jejuro.miraero.domain.user.service.UserService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.exception.GlobalExceptionHandler;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.jejuro.miraero.global.security.JwtAuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  private static final Long USER_ID = 1L;

  @Mock
  private UserService userService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    UserController userController = new UserController(userService);

    mockMvc = MockMvcBuilders
        .standaloneSetup(userController)
        .setControllerAdvice(new GlobalExceptionHandler())
        .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
        .build();

    SecurityContextHolder.getContext().setAuthentication(
        new JwtAuthenticationToken(new AuthenticatedUser(USER_ID))
    );
  }

  @Test
  @DisplayName("프로필 조회 요청은 200 응답과 사용자 프로필을 반환한다")
  void getProfile_success() throws Exception {
    ProfileResponse response = ProfileResponse.builder()
        .userId(USER_ID)
        .name("김미래")
        .email("miraero@gmail.com")
        .birthDate("2001-03-15")
        .profileImageUrl(null)
        .company("KB금융그룹")
        .monthlyIncome(2_850_000L)
        .kbpayLinked(true)
        .build();
    given(userService.getProfile(USER_ID)).willReturn(response);

    mockMvc.perform(get("/api/users/profile"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.userId").value(USER_ID))
        .andExpect(jsonPath("$.data.name").value("김미래"))
        .andExpect(jsonPath("$.data.email").value("miraero@gmail.com"))
        .andExpect(jsonPath("$.data.birthDate").value("2001-03-15"))
        .andExpect(jsonPath("$.data.profileImageUrl").doesNotExist())
        .andExpect(jsonPath("$.data.company").value("KB금융그룹"))
        .andExpect(jsonPath("$.data.monthlyIncome").value(2_850_000L))
        .andExpect(jsonPath("$.data.kbpayLinked").value(true));

    verify(userService).getProfile(USER_ID);
  }

  @Test
  @DisplayName("조회할 사용자가 없으면 404 응답을 반환한다")
  void getProfile_notFound() throws Exception {
    given(userService.getProfile(USER_ID))
        .willThrow(new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND));

    mockMvc.perform(get("/api/users/profile"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("COMMON_004"));

    verify(userService).getProfile(USER_ID);
  }
}
