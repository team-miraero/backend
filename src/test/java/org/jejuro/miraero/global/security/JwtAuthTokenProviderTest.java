package org.jejuro.miraero.global.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
