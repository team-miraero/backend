package org.jejuro.miraero.domain.transaction.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.transaction.domain.TransactionQueryResult;
import org.jejuro.miraero.domain.transaction.dto.request.TransactionSearchCondition;

public interface TransactionMapper {

    List<TransactionQueryResult> findTransactions(
            @Param("userId") Long userId,
            @Param("condition") TransactionSearchCondition condition
    );

    long countTransactions(
            @Param("userId") Long userId,
            @Param("condition") TransactionSearchCondition condition
    );
}
