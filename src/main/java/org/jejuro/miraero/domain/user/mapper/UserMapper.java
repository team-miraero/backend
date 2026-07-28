package org.jejuro.miraero.domain.user.mapper;

import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.user.model.User;

public interface UserMapper {

  //이메일 중복 확인
  boolean existsByEmail(@Param("email") String email);

  int save(User user);
}
