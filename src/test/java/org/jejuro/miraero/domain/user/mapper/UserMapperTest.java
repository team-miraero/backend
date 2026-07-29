package org.jejuro.miraero.domain.user.mapper;


import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.jejuro.miraero.domain.user.model.User;
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
}
