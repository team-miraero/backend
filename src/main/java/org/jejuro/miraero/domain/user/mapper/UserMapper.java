package org.jejuro.miraero.domain.user.mapper;

import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.dto.response.ProfileResponse;

public interface UserMapper {

  //이메일 중복 확인
  boolean existsByEmail(@Param("email") String email);

  int save(User user);

  User findByEmail(@Param("email") String email);

  User findById(@Param("userId") Long userId);

  ProfileResponse findProfileById(@Param("userId") Long userId);
}
