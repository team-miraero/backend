package org.jejuro.miraero.domain.mydata.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.mydata.client.MyDataAuthClient;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataTokenResponse;
import org.jejuro.miraero.domain.mydata.dto.response.MyDataConnectResponse;
import org.jejuro.miraero.domain.mydata.exception.MyDataErrorCode;
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

  private final MyDataAuthClient myDataAuthClient;
  private final MyDataTokenRepository myDataTokenRepository;
  private final MyDataSyncService myDataSyncService;
  private final UserMapper userMapper;

  @Override
  @Transactional
  public MyDataConnectResponse connect(Long userId) {
    User user = userMapper.findById(userId);
    if (user == null) {
      throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
    }

    String authorizationCode = myDataAuthClient.requestAuthorizationCode(user.getEmail());
    MyDataTokenResponse token = myDataAuthClient.exchangeToken(authorizationCode);

    myDataTokenRepository.save(userId, token.getAccessToken(), token.getExpiresIn());
    userMapper.updateKbPayId(userId, token.getKbUserId());

    return new MyDataConnectResponse(token.getKbUserId());
  }

  @Override
  public void sync(Long userId) {
    User user = userMapper.findById(userId);
    if (user == null) {
      throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
    }
    // 연동(kb_pay_id 저장) 이전에는 동기화할 외부 계정이 없으므로 여기서 막는다
    if (user.getKbPayId() == null) {
      throw new BusinessException(MyDataErrorCode.MYDATA_NOT_CONNECTED);
    }

    myDataSyncService.sync(userId, user.getKbPayId());
  }
}
