package org.jejuro.miraero.global.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthTokenProvider implements AuthTokenProvider {

  private static final String TOKEN_TYPE_CLAIM = "tokenType";
  private static final String ACCESS_TOKEN_TYPE = "ACCESS";
  private static final String REFRESH_TOKEN_TYPE = "REFRESH";
  private static final int MILLISECONDS_PER_SECOND = 1000;

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.access-token-expires-in}")
  private Long accessTokenExpiresIn;

  @Value("${jwt.refresh-token-expires-in}")
  private Long refreshTokenExpiresIn;

  private Key signingKey;

  @PostConstruct
  public void init() {
    signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public String createAccessToken(Long userId, String email) {
    return createToken(userId, email, ACCESS_TOKEN_TYPE, accessTokenExpiresIn);
  }

  @Override
  public String createRefreshToken(Long userId, String email) {
    return createToken(userId, email, REFRESH_TOKEN_TYPE, refreshTokenExpiresIn);

  }

  @Override
  public Long getAccessTokenExpiresIn() {
    return accessTokenExpiresIn;
  }

  @Override
  public Long getRefreshTokenExpiresIn() {
    return refreshTokenExpiresIn;
  }

  private String createToken(
      Long userId,
      String email,
      String tokenType,
      Long expiresIn
  ) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + expiresIn * MILLISECONDS_PER_SECOND);

    return Jwts.builder()
        .setSubject(String.valueOf(userId))
        .claim("email", email)
        .claim(TOKEN_TYPE_CLAIM, tokenType)
        .setIssuedAt(now)
        .setExpiration(expiration)
        .signWith(signingKey, SignatureAlgorithm.HS256)
        .compact();
  }
}
