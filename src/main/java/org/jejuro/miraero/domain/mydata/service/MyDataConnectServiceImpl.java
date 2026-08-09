package org.jejuro.miraero.domain.mydata.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.mydata.client.MyDataAuthClient;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataTokenResponse;
import org.jejuro.miraero.domain.mydata.dto.response.MyDataConnectResponse;
import org.jejuro.miraero.domain.mydata.exception.MyDataErrorCode;
import org.jejuro.miraero.domain.mydata.mapper.MyDataConsentMapper;
import org.jejuro.miraero.domain.mydata.mapper.ReferenceDataMapper;
import org.jejuro.miraero.domain.mydata.repository.MyDataTokenRepository;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyDataConnectServiceImpl implements MyDataConnectService {

  // mock-server가 지금은 국민은행 하나만 시뮬레이션하므로 고정 코드로 조회한다
  private static final String MOCK_INSTITUTION_CODE = "004";
  private static final String CONNECTION_STATUS_CONNECTED = "CONNECTED";

  private final MyDataAuthClient myDataAuthClient;
  private final MyDataTokenRepository myDataTokenRepository;
  private final MyDataSyncService myDataSyncService;
  private final UserMapper userMapper;
  private final ReferenceDataMapper referenceDataMapper;
  private final MyDataConsentMapper myDataConsentMapper;

  @Override
  @Transactional
  public MyDataConnectResponse connect(Long userId) {
    User user = userMapper.findById(userId);
    if (user == null) {
      throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
    }

    MyDataTokenResponse token = authenticateAndPersist(userId, user);

    return new MyDataConnectResponse(token.getKbUserId());
  }

  @Override
  @Transactional
  public void sync(Long userId) {
    User user = userMapper.findById(userId);
    if (user == null) {
      throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
    }
    // 연동(kb_pay_id 저장) 이전에는 동기화할 외부 계정이 없으므로 여기서 막는다
    if (user.getKbPayId() == null) {
      throw new BusinessException(MyDataErrorCode.MYDATA_NOT_CONNECTED);
    }

    syncWithTokenRefresh(userId, user);

    Long financialInstitutionId = referenceDataMapper.findFinancialInstitutionIdByCode(MOCK_INSTITUTION_CODE);
    myDataConsentMapper.updateSyncedAt(userId, financialInstitutionId);
  }

  // 토큰이 Redis TTL로 만료됐으면 재인증 후 1회만 재시도한다 (사용자에게 재연동을 요구하지 않기 위함)
  private void syncWithTokenRefresh(Long userId, User user) {
    try {
      myDataSyncService.sync(userId, user.getKbPayId());
    } catch (BusinessException exception) {
      if (exception.getErrorCode() != MyDataErrorCode.MYDATA_TOKEN_EXPIRED) {
        throw exception;
      }
      authenticateAndPersist(userId, user);
      myDataSyncService.sync(userId, user.getKbPayId());
    }
  }

  private MyDataTokenResponse authenticateAndPersist(Long userId, User user) {
    String authorizationCode = myDataAuthClient.requestAuthorizationCode(user.getEmail());
    MyDataTokenResponse token = myDataAuthClient.exchangeToken(authorizationCode);

    myDataTokenRepository.save(userId, token.getAccessToken(), token.getExpiresIn());
    userMapper.updateKbPayId(userId, token.getKbUserId());

    Long financialInstitutionId = referenceDataMapper.findFinancialInstitutionIdByCode(MOCK_INSTITUTION_CODE);
    myDataConsentMapper.upsertConnection(
        userId,
        financialInstitutionId,
        CONNECTION_STATUS_CONNECTED,
        LocalDateTime.now().plusSeconds(token.getExpiresIn())
    );

    return token;
  }
}
