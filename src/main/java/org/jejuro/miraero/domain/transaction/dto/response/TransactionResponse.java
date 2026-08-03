package org.jejuro.miraero.domain.transaction.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TransactionResponse {

    private Long transactionId;
    private String transactionType;
    private String merchantName;
    private Long amount;
    private Long balanceAfter;
    private LocalDateTime transactedAt;
    private ExpenseCategoryResponse category;

    public static TransactionResponse of(
            Long transactionId,
            String transactionType,
            String merchantName,
            Long amount,
            Long balanceAfter,
            LocalDateTime transactedAt,
            ExpenseCategoryResponse category
    ) {
        return new TransactionResponse(
                transactionId,
                transactionType,
                merchantName,
                amount,
                balanceAfter,
                transactedAt,
                category
        );
    }
}
