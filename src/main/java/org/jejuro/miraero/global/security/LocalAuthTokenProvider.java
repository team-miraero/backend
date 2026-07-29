package org.jejuro.miraero.global.security;

import java.util.UUID;

public class LocalAuthTokenProvider implements AuthTokenProvider {

    private static final Long ACCESS_TOKEN_EXPIRES_IN = 1800L;
    private static final Long REFRESH_TOKEN_EXPIRES_IN = 1209600L;
    private static final String ACCESS_TOKEN_PREFIX = "local-access-token";
    private static final String REFRESH_TOKEN_PREFIX = "local-refresh-token";

    @Override
    public String createAccessToken(Long userId, String email) {
        return createLocalToken(ACCESS_TOKEN_PREFIX, userId);
    }

    @Override
    public String createRefreshToken(Long userId, String email) {
        return createLocalToken(REFRESH_TOKEN_PREFIX, userId);
    }

    @Override
    public Long getAccessTokenExpiresIn() {
        return ACCESS_TOKEN_EXPIRES_IN;
    }

    @Override
    public Long getRefreshTokenExpiresIn() {
        return REFRESH_TOKEN_EXPIRES_IN;
    }

    private String createLocalToken(String prefix, Long userId) {
        return prefix + "-" + userId + "-" + UUID.randomUUID();
    }
}
