package org.jejuro.miraero.domain.moneybox.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBox;
import org.jejuro.miraero.domain.moneybox.dto.request.MoneyBoxCreateRequest;
import org.jejuro.miraero.domain.moneybox.dto.response.MoneyBoxCreateResponse;
import org.jejuro.miraero.domain.moneybox.mapper.MoneyBoxMapper;
import org.jejuro.miraero.domain.moneybox.util.MoneyBoxAccountNumberGenerator;
import org.jejuro.miraero.global.crypto.AccountNumberCrypto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MoneyBoxServiceImpl implements MoneyBoxService {

    private final MoneyBoxMapper moneyBoxMapper;
    private final MoneyBoxAccountNumberGenerator moneyBoxAccountNumberGenerator;
    private final AccountNumberCrypto accountNumberCrypto;

    @Override
    @Transactional
    public MoneyBoxCreateResponse createMoneyBox(Long userId, MoneyBoxCreateRequest request) {

        String accountNumber =
                generateUniqueAccountNumber();

        MoneyBox moneyBox =
                MoneyBox.builder()
                        .userId(userId)
                        .balance(0L)
                        .moneyBoxType(request.getMoneyBoxType())
                        .accountNumber(accountNumberCrypto.encrypt(accountNumber))
                        .accountNumberHash(accountNumberCrypto.hashForMoneyBox(accountNumber))
                        .maskedAccountNumber(accountNumberCrypto.mask(accountNumber))
                        .build();

        moneyBoxMapper.insert(moneyBox);

//        if (request.getAutoTransfer() != null) {
//            autoTransferService.create(
//                    moneyBox.getMoneyBoxId(),
//                    userId,
//                    request.getAutoTransfer()
//            );
//        }

        return MoneyBoxCreateResponse.builder()
                .moneyBoxId(moneyBox.getMoneyBoxId())
                .moneyBoxType(moneyBox.getMoneyBoxType())
                .build();
    }

    private String generateUniqueAccountNumber() {

        while (true) {
            String accountNumber = moneyBoxAccountNumberGenerator.generate();

            String hash = accountNumberCrypto.hashForMoneyBox(accountNumber);

            if (!moneyBoxMapper.existsByAccountNumberHash(hash)) {
                return accountNumber;
            }
        }
    }
}
