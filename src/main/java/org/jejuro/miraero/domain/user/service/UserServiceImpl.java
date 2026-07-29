package org.jejuro.miraero.domain.user.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.mydata.service.MyDataConsentService;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.dto.request.UserSignUpRequest;
import org.jejuro.miraero.domain.user.dto.response.UserSignUpResponse;
import org.jejuro.miraero.domain.user.exception.UserErrorCode;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final MyDataConsentService myDataConsentService;

  @Transactional
  public UserSignUpResponse signUp(UserSignUpRequest request) {

    //이메일 중복 체크
    boolean exists = userMapper.existsByEmail(request.getEmail());

    if (exists) {
      throw new BusinessException(
          UserErrorCode.EMAIL_ALREADY_EXISTS
      );
    }

    String passwordHash = passwordEncoder.encode(request.getPassword());

    //TODO 목서버 연동 후 실제 사용자 정보로 교체
    User user = User.create(
        "테스트 사용자",
        LocalDate.of(2000, 1, 1),
        "테스트 회사",
        3_000_000L,
        request.getEmail(),
        passwordHash,
        1L
    );

    userMapper.save(user);
    myDataConsentService.createInitialConsent(user.getUserId());
    return new UserSignUpResponse(
        user.getUserId(),
        user.getName(),
        user.getEmail()
    );
  }
}
