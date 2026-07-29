package org.jejuro.miraero.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.auth.dto.request.LoginRequest;
import org.jejuro.miraero.domain.auth.dto.request.SignUpRequest;
import org.jejuro.miraero.domain.auth.dto.response.LoginResponse;
import org.jejuro.miraero.domain.auth.dto.response.LoginUserResponse;
import org.jejuro.miraero.domain.auth.dto.response.SignUpResponse;
import org.jejuro.miraero.domain.auth.dto.response.TokenResponse;
import org.jejuro.miraero.domain.auth.exception.AuthErrorCode;
import org.jejuro.miraero.domain.mydata.service.MyDataLinkService;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.domain.user.service.UserCreateCommand;
import org.jejuro.miraero.domain.user.service.UserService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.security.AuthTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_TYPE = "Bearer";
    private static final boolean AUTO_LOGIN = true;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MyDataLinkService myDataLinkService;
    private final AuthTokenProvider authTokenProvider;
    private final UserService userService;

    @Override
    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        User user = userService.create(
            new UserCreateCommand(
                request.getEmail(),
                request.getPassword()
            )
        );

        return SignUpResponse.from(user);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByEmail(request.getEmail());

        if (user == null) {
            throw new BusinessException(AuthErrorCode.INVALID_EMAIL_OR_PASSWORD);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(AuthErrorCode.INVALID_EMAIL_OR_PASSWORD);
        }

        myDataLinkService.syncUserData(user);

        String accessToken = authTokenProvider.createAccessToken(
            user.getUserId(),
            user.getEmail()
        );

        String refreshToken = authTokenProvider.createRefreshToken(
            user.getUserId(),
            user.getEmail()
        );

        TokenResponse token = new TokenResponse(
            accessToken,
            refreshToken,
            TOKEN_TYPE,
            authTokenProvider.getAccessTokenExpiresIn(),
            authTokenProvider.getRefreshTokenExpiresIn()
        );

        return new LoginResponse(
            token,
            AUTO_LOGIN,
            LoginUserResponse.from(user)
        );
    }
}
