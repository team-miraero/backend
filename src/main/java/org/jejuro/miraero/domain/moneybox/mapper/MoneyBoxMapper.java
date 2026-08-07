package org.jejuro.miraero.domain.moneybox.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBox;

@Mapper
public interface MoneyBoxMapper {

  void insert(MoneyBox moneyBox);

  boolean existsByAccountNumberHash(
      @Param("accountNumberHash") String accountNumberHash);

  int decreaseBalance(
      @Param("moneyBoxId") Long moneyBoxId,
      @Param("userId") Long userId,
      @Param("amount") Long amount
  );

  MoneyBox findPaceMakerMoneyBoxByIdAndUserIdForUpdate(
      @Param("moneyBoxId") Long moneyBoxId,
      @Param("userId") Long userId
  );
}
