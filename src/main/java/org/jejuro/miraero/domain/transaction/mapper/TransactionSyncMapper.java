package org.jejuro.miraero.domain.transaction.mapper;

import org.jejuro.miraero.domain.transaction.domain.TransactionSyncCommand;

// org.jejuro.miraero.domain.transaction.mapper는 이미 MyBatisConfig의 @MapperScan 대상이라
// 별도 @Mapper 어노테이션 없이도 스캔된다 (TransactionMapper와 동일한 방식).
public interface TransactionSyncMapper {

  int upsert(TransactionSyncCommand command);
}
