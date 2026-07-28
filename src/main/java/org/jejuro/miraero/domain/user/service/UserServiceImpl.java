package org.jejuro.miraero.domain.user.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.user.dto.request.UserSignUpRequest;
import org.jejuro.miraero.domain.user.dto.response.UserSignUpResponse;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.domain.user.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  public UserSignUpResponse signUp(UserSignUpRequest request) {

    //이메일 중복 체크
    boolean exists = userMapper.existsByEmail(request.getEmail());

    if (exists) {
      throw new IllegalArgumentException("이미 가입된 이메일입니다.");
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

    return new UserSignUpResponse(
        user.getUserId(),
        user.getName(),
        user.getEmail()
    );
  }
}
