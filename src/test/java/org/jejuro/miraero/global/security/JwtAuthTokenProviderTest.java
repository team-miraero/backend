package org.jejuro.miraero.global.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtAuthTokenProviderTest {

  private static final String SECRET =
      "test-jwt-secret-key-for-miraero-local-test-1234567890";

  @Test
  @DisplayName("Access Token을 발급한다")
  void createAccessToken() {
    JwtAuthTokenProvider provider = createProvider();

    String token = provider.createAccessToken(1L, "test@example.com");

    Claims claims = parseClaims(token);

    assertNotNull(token);
    assertEquals("1", claims.getSubject());
    assertEquals("test@example.com", claims.get("email"));
    assertEquals("ACCESS", claims.get("tokenType"));
  }

  @Test
  @DisplayName("Refresh Token을 발급한다")
  void createRefreshToken() {
    JwtAuthTokenProvider provider = createProvider();

    String token = provider.createRefreshToken(1L, "test@example.com");

    Claims claims = parseClaims(token);

    assertNotNull(token);
    assertEquals("1", claims.getSubject());
    assertEquals("test@example.com", claims.get("email"));
    assertEquals("REFRESH", claims.get("tokenType"));
  }

  @Test
  @DisplayName("토큰 만료 시간을 반환한다")
  void getExpiresIn() {
    JwtAuthTokenProvider provider = createProvider();

    assertEquals(1800L, provider.getAccessTokenExpiresIn());
    assertEquals(1209600L, provider.getRefreshTokenExpiresIn());
  }

  @Test
  @DisplayName("유효한 토큰이면 true를 반환한다")
  void validateToken_validToken() {
    JwtAuthTokenProvider provider = createProvider();
    String token = provider.createAccessToken(1L, "test@example.com");

    boolean valid = provider.validateToken(token);

    assertTrue(valid);
  }

  @Test
  @DisplayName("잘못된 토큰이면 false를 반환한다")
  void validateToken_invalidToken() {
    JwtAuthTokenProvider provider = createProvider();

    boolean valid = provider.validateToken("invalid-token");

    assertFalse(valid);
  }

  @Test
  @DisplayName("토큰에서 userId와 email을 추출한다")
  void getUserInfo() {
    JwtAuthTokenProvider provider = createProvider();
    String token = provider.createAccessToken(1L, "test@example.com");

    Long userId = provider.getUserId(token);
    String email = provider.getEmail(token);

    assertEquals(1L, userId);
    assertEquals("test@example.com", email);
  }

  @Test
  @DisplayName("Access Token 여부를 확인한다")
  void isAccessToken() {
    JwtAuthTokenProvider provider = createProvider();
    String accessToken = provider.createAccessToken(1L, "test@example.com");
    String refreshToken = provider.createRefreshToken(1L, "test@example.com");

    assertTrue(provider.isAccessToken(accessToken));
    assertFalse(provider.isAccessToken(refreshToken));
  }

  private JwtAuthTokenProvider createProvider() {
    JwtAuthTokenProvider provider = new JwtAuthTokenProvider();

    ReflectionTestUtils.setField(provider, "secret", SECRET);
    ReflectionTestUtils.setField(provider, "accessTokenExpiresIn", 1800L);
    ReflectionTestUtils.setField(provider, "refreshTokenExpiresIn", 1209600L);

    provider.init();

    return provider;
  }

  private Claims parseClaims(String token) {
    Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }
}
