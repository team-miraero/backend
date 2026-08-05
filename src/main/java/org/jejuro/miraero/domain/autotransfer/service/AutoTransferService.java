package org.jejuro.miraero.domain.autotransfer.service;


import org.jejuro.miraero.domain.autotransfer.dto.request.AutoTransferCreateRequest;
import org.jejuro.miraero.domain.autotransfer.dto.response.AutoTransferResponse;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBox;

public interface AutoTransferService {
    void createMoneyBoxAutoTransfer(
            Long userId,
            Long moneyBoxId,
            String maskedDepositAccount,
            AutoTransferCreateRequest request
    );
}
