package org.jejuro.miraero.domain.account.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.account.domain.Account;

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

  // ex_account_id 기준 upsert — 동기화 재실행 시 중복 없이 갱신
  int upsert(Account account);

  // 외부 계좌 ID로 내부 account_id 조회 (거래 동기화 시 FK 연결용)
  Long findAccountIdByExAccountId(@Param("exAccountId") Long exAccountId);
}
