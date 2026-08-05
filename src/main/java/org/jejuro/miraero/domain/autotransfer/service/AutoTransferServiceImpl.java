package org.jejuro.miraero.domain.autotransfer.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.account.Account;
import org.jejuro.miraero.domain.autotransfer.domain.AutoTransfer;
import org.jejuro.miraero.domain.autotransfer.domain.AutoTransferStatus;
import org.jejuro.miraero.domain.autotransfer.dto.request.AutoTransferCreateRequest;
import org.jejuro.miraero.domain.autotransfer.mapper.AutoTransferMapper;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBox;
import org.jejuro.miraero.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class AutoTransferServiceImpl implements AutoTransferService {

    private final AutoTransferMapper autoTransferMapper;


    @Override
    @Transactional
    public void createMoneyBoxAutoTransfer(
            Long userId,
            Long moneyBoxId,
            String maskedDepositAccount,
            AutoTransferCreateRequest request
    ){
//        Account account =
//                accountMapper.findByIdAndUserId(
//                        request.getWithdrawalAccountId(),
//                        userId
//                );
//
//        if (account == null) {
//            throw new BusinessException(
//                    AccountErrorCode.ACCOUNT_NOT_FOUND
//            );
//        }



        AutoTransfer autoTransfer =
                AutoTransfer.builder()
                        .withdrawalAccountId(
                                request.getWithdrawalAccountId()
                        )
                        .moneyBoxId(
                                moneyBoxId
                        )
                        .maskedDepositAccount(
                                maskedDepositAccount
                        )
                        .transferAmount(
                                request.getAmount()
                        )
                        .transferDay(
                                request.getTransferDay()
                        )
                        .startDate(
                                LocalDate.now()
                        )
                        .autoTransferStatus(
                                AutoTransferStatus.ACTIVE
                        )
                        .syncedAt(
                                LocalDateTime.now()
                        )
                        .build();


        autoTransferMapper.save(
                autoTransfer
        );


    }

}
