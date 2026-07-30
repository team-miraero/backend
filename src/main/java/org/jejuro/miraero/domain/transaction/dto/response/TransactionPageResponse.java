package org.jejuro.miraero.domain.transaction.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TransactionPageResponse {

    private List<TransactionResponse> transactions;
    private PaginationResponse pagination;

    public static TransactionPageResponse of(
            List<TransactionResponse> transactions,
            PaginationResponse pagination
    ) {
        return new TransactionPageResponse(transactions, pagination);
    }
}
