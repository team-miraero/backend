package org.jejuro.miraero.global.security;

public interface AuthTokenProvider {

  String createAccessToken(Long userId, String email);

  String createRefreshToken(Long userId, String email);

  Long getAccessTokenExpiresIn();

  Long getRefreshTokenExpiresIn();
}
