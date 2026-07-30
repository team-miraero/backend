package org.jejuro.miraero.domain.mydata.service;

import org.jejuro.miraero.domain.user.domain.User;
import org.springframework.stereotype.Service;

@Service
public class MyDataLinkServiceImpl implements MyDataLinkService {

  @Override
  public void syncUserData(User user) {
    // TODO 서버 구축 후 실제 MyData/KB Pay 연동으로 교체
  }
}
