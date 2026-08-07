package org.jejuro.miraero.domain.mydata.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.mydata.domain.MyDataConsent;
import org.jejuro.miraero.domain.mydata.dto.MyDataConnectionResponse;

public interface MyDataConsentMapper {

  int save(MyDataConsent consent);

  List<MyDataConnectionResponse> findConnectionsByUserId(@Param("userId") Long userId);
}
