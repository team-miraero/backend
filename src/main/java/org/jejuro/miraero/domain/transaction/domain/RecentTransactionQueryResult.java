package org.jejuro.miraero.domain.transaction.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecentTransactionQueryResult {

    private Long transactionId;
    private String transactionName;
    private Long categoryId;
    private String categoryName;
    private Long amount;
    private LocalDateTime transactedAt;
}
