package org.jejuro.miraero.global.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.jejuro.miraero.global.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringJUnitConfig(SecurityConfigTest.TestConfig.class)
@WebAppConfiguration
class SecurityConfigTest {

  private final MockMvc mockMvc;
  private final AuthTokenProvider authTokenProvider;

  @Autowired
  SecurityConfigTest(
      WebApplicationContext webApplicationContext,
      SecurityFilterChain securityFilterChain,
      AuthTokenProvider authTokenProvider
  ) {
    this.authTokenProvider = authTokenProvider;
    this.mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .apply(
            SecurityMockMvcConfigurers.springSecurity(
                new FilterChainProxy(securityFilterChain)
            )
        )
        .build();
  }

  @Test
  @DisplayName("/health는 토큰 없이 접근할 수 있다")
  void publicApi_withoutToken() throws Exception {
    mockMvc.perform(get("/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").value("ok"));
  }

  @Test
  @DisplayName("보호 API는 토큰이 없으면 401을 반환한다")
  void protectedApi_withoutToken() throws Exception {
    mockMvc.perform(get("/api/test/protected"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("보호 API는 유효한 Access Token이면 접근할 수 있다")
  void protectedApi_withAccessToken() throws Exception {
    when(authTokenProvider.validateToken("access-token"))
        .thenReturn(true);
    when(authTokenProvider.isAccessToken("access-token"))
        .thenReturn(true);
    when(authTokenProvider.getUserId("access-token"))
        .thenReturn(1L);
    when(authTokenProvider.getEmail("access-token"))
        .thenReturn("test@example.com");

    mockMvc.perform(
            get("/api/test/protected")
                .header("Authorization", "Bearer access-token")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").value("authenticated"));
  }

  @Test
  @DisplayName("보호 API는 Refresh Token이면 401을 반환한다")
  void protectedApi_withRefreshToken() throws Exception {
    when(authTokenProvider.validateToken("refresh-token"))
        .thenReturn(true);
    when(authTokenProvider.isAccessToken("refresh-token"))
        .thenReturn(false);

    mockMvc.perform(
            get("/api/test/protected")
                .header("Authorization", "Bearer refresh-token")
        )
        .andExpect(status().isUnauthorized());
  }

  @Configuration
  @EnableWebMvc
  @Import(SecurityConfig.class)
  static class TestConfig {

    @Bean
    AuthTokenProvider authTokenProvider() {
      return Mockito.mock(AuthTokenProvider.class);
    }

    @Bean
    JwtAuthenticationFilter jwtAuthenticationFilter(
        AuthTokenProvider authTokenProvider
    ) {
      return new JwtAuthenticationFilter(authTokenProvider);
    }

    @Bean
    TestProtectedController testProtectedController() {
      return new TestProtectedController();
    }
  }
}
