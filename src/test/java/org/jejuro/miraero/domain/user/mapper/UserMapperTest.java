package org.jejuro.miraero.domain.user.mapper;


import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.global.config.DataSourceConfig;
import org.jejuro.miraero.global.config.MyBatisConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DataSourceConfig.class,
    MyBatisConfig.class
})

@Transactional
public class UserMapperTest {

  @Autowired
  private UserMapper userMapper;

  @Test
  @DisplayName("회원을 저장하고 이메일 존재 여부를 확인한다")
  void saveAndExistsByEmail() {
    // given
    User user = User.create(
        "홍길동",
        LocalDate.of(2000, 1, 1),
        "미래로",
        3_000_000L,
        "mapper-test@example.com",
        "encodedPassword",
        100L
    );

    // when
    int result = userMapper.save(user);
    boolean exists = userMapper.existsByEmail(user.getEmail());

    // then
    assertEquals(1, result);
    assertNotNull(user.getUserId());
    assertTrue(exists);
  }

  @Test
  @DisplayName("이메일로 회원을 조회한다.")
  void findByEmail() {
    //given
    User user = User.create(
        "테스트 사용자",
        LocalDate.of(2000, 1, 1),
        "테스트 회사",
        3_000_000L,
        "login-test@example.com",
        "encodedPassword",
        1L
    );

    userMapper.save(user);

    //when
    User foundUser = userMapper.findByEmail("login-test@example.com");

    //then
    assertNotNull(foundUser);
    assertEquals(user.getUserId(), foundUser.getUserId());
    assertEquals("테스트 사용자", foundUser.getName());
    assertEquals("login-test@example.com", foundUser.getEmail());
  }

  @Test
  @DisplayName("존재하지 않는 이메일이면 null을 반환한다")
  void findByEmailNull() {
    //when
    User foundUser = userMapper.findByEmail("not-found@example.com");

    //then
    assertNull(foundUser);
  }
}
