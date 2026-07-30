package org.jejuro.miraero.domain.transaction.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecentTransactionResponse {
    private Long transactionId;
    private String transactionName;
    private Long categoryId;
    private String categoryName;
    private Long amount;
    private LocalDateTime transactedAt;
}
