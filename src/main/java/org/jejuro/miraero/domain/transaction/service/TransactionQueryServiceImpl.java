package org.jejuro.miraero.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.transaction.mapper.TransactionMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionQueryServiceImpl implements TransactionQueryService {

    private final TransactionMapper transactionMapper;

    @Override
    public List<LocalDateTime> getLatestSalaryDateTimes(Long userId, int limit) {
        return transactionMapper.findLatestSalaryDateTimes(userId, limit);
    }

    @Override
    public Long getFixedExpenseSum(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        Long sum = transactionMapper.findFixedExpenseSum(userId, startDate, endDate);
        return sum == null ? 0L : sum;
    }

    @Override
    public Long getVariableExpenseSum(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        Long sum = transactionMapper.findVariableExpenseSum(userId, startDate, endDate);
        return sum == null ? 0L : sum;
    }

    @Override
    public Long getTodayExpenseSum(Long userId) {
        Long sum = transactionMapper.findTodayExpenseSum(userId);
        return sum == null ? 0L : sum;
    }
}
