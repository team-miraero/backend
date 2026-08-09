package org.jejuro.miraero.domain.transaction.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.jejuro.miraero.domain.transaction.domain.TransactionSyncCommand;
import org.jejuro.miraero.global.config.RootConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

@SpringJUnitConfig
@ContextConfiguration(classes = RootConfig.class)
@Transactional
@Rollback
class TransactionSyncMapperTest {

  @Autowired
  private TransactionSyncMapper transactionSyncMapper;

  @Test
  @DisplayName("같은 ex_transaction_id로 두 번 upsert해도 오류 없이 갱신된다")
  void upsert_isIdempotent() {
    TransactionSyncCommand command = new TransactionSyncCommand(
        1L,
        null,
        null,
        999301L,
        "WITHDRAWAL",
        5500L,
        3394500L,
        LocalDateTime.of(2026, 8, 1, 9, 12),
        "스타벅스"
    );

    assertEquals(1, transactionSyncMapper.upsert(command));
    transactionSyncMapper.upsert(command);
  }
}
