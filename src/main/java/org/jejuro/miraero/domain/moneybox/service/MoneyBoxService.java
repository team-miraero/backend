package org.jejuro.miraero.domain.moneybox.service;


import org.jejuro.miraero.domain.moneybox.dto.request.MoneyBoxCreateRequest;
import org.jejuro.miraero.domain.moneybox.dto.response.MoneyBoxCreateResponse;

public interface MoneyBoxService {
    MoneyBoxCreateResponse createMoneyBox(
            Long userId,
            MoneyBoxCreateRequest request
    );


}
