package org.jejuro.miraero.domain.account.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AccountMapper {

  Long findBalanceByIdAndUserIdForUpdate(
      @Param("accountId") Long accountId,
      @Param("userId") Long userId
  );

  int increaseBalance(
      @Param("accountId") Long accountId,
      @Param("userId") Long userId,
      @Param("amount") Long amount
  );
}
