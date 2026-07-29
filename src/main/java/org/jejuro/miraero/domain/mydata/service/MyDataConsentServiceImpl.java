package org.jejuro.miraero.domain.mydata.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.mydata.domain.MyDataConsent;
import org.jejuro.miraero.domain.mydata.mapper.MyDataConsentMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyDataConsentServiceImpl implements MyDataConsentService {

  private static final String INITIAL_TERMS_VERSION = "v1.0";
  private static final String AGREED_STATUS = "AGREED";
  private static final int CONSENT_VALID_YEARS = 1;

  private final MyDataConsentMapper myDataConsentMapper;

  public void createInitialConsent(Long userId) {
    LocalDateTime agreedAt = LocalDateTime.now();
    LocalDateTime expiresAt = agreedAt.plusYears(CONSENT_VALID_YEARS);

    MyDataConsent myDataConsent = MyDataConsent.create(
        userId,
        INITIAL_TERMS_VERSION,
        agreedAt,
        expiresAt,
        AGREED_STATUS
    );

    myDataConsentMapper.save(myDataConsent);
  }
}
