package org.jejuro.miraero.domain.moneybox.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.account.domain.Account;
import org.jejuro.miraero.domain.account.exception.AccountErrorCode;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.domain.autotransfer.service.AutoTransferService;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBox;
import org.jejuro.miraero.domain.moneybox.dto.request.MoneyBoxCreateRequest;
import org.jejuro.miraero.domain.moneybox.dto.response.MoneyBoxCreateResponse;
import org.jejuro.miraero.domain.moneybox.exception.MoneyBoxErrorCode;
import org.jejuro.miraero.domain.moneybox.mapper.MoneyBoxMapper;
import org.jejuro.miraero.domain.transaction.service.TransactionQueryService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MoneyBoxServiceImpl implements MoneyBoxService {

    private static final String CHECKING = "CHECKING";

    private final MoneyBoxMapper moneyBoxMapper;
    private final AccountMapper accountMapper;
    private final AutoTransferService autoTransferService;
    private final TransactionQueryService transactionQueryService;

    @Override
    @Transactional
    public MoneyBoxCreateResponse createMoneyBox(Long userId, MoneyBoxCreateRequest request) {

        Account account = accountMapper.findByIdAndUserId(request.getAccountId(), userId);

        if (account == null) {
            throw new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND);
        }

        // 저금통은 계좌 잔액의 일부를 묶어두는 구획이라 입출금통장에만 만들 수 있다
        if (!CHECKING.equals(account.getAccountType())) {
            throw new BusinessException(MoneyBoxErrorCode.INVALID_MONEY_BOX_ACCOUNT);
        }

        MoneyBox moneyBox =
                MoneyBox.builder()
                        .userId(userId)
                        .accountId(account.getAccountId())
                        .balance(0L)
                        .moneyBoxType(request.getMoneyBoxType())
                        .build();

        moneyBoxMapper.insert(moneyBox);

        if (request.getAutoTransfer() != null) {
            validateSalaryAccount(userId, account.getAccountId());

            autoTransferService.createMoneyBoxAutoTransfer(
                    userId,
                    moneyBox.getMoneyBoxId(),
                    account.getAccountId(),
                    account.getMaskedAccountNumber(),
                    request.getAutoTransfer()
            );
        }

        return MoneyBoxCreateResponse.builder()
                .moneyBoxId(moneyBox.getMoneyBoxId())
                .accountId(moneyBox.getAccountId())
                .moneyBoxType(moneyBox.getMoneyBoxType())
                .build();
    }

    /**
     * 자동이체는 같은 계좌 안에서 금액을 묶는 동작이라, 매달 입금이 들어오는 계좌가 아니면
     * 잔액이 고갈된다. 급여 계좌를 특정할 수 없는 경우(프리랜서 등)에는 제한하지 않는다.
     */
    private void validateSalaryAccount(Long userId, Long accountId) {
        Long salaryAccountId = transactionQueryService.getSalaryAccountId(userId);

        if (salaryAccountId != null && !salaryAccountId.equals(accountId)) {
            throw new BusinessException(MoneyBoxErrorCode.AUTO_TRANSFER_NOT_SALARY_ACCOUNT);
        }
    }
}
