package org.jejuro.miraero.domain.transaction.service;

import org.jejuro.miraero.domain.transaction.dto.request.TransactionSearchCondition;
import org.jejuro.miraero.domain.transaction.dto.response.TransactionPageResponse;

public interface TransactionService {

    TransactionPageResponse getTransactions(Long userId, TransactionSearchCondition condition);
}
