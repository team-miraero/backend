package org.jejuro.miraero.domain.mydata.controller;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.mydata.dto.MyDataConnectionListResponse;
import org.jejuro.miraero.domain.mydata.dto.response.MyDataConnectResponse;
import org.jejuro.miraero.domain.mydata.service.MyDataConnectService;
import org.jejuro.miraero.domain.mydata.service.MyDataConsentService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/mydata")
@RequiredArgsConstructor
public class MyDataController {

  private final MyDataConsentService myDataConsentService;
  private final MyDataConnectService myDataConnectService;

  @GetMapping("/connections")
  public ResponseEntity<ApiResponse<MyDataConnectionListResponse>> getConnections(
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    MyDataConnectionListResponse response =
        myDataConsentService.getConnections(user.getUserId());

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PostMapping("/connect")
  public ResponseEntity<ApiResponse<MyDataConnectResponse>> connect(
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    MyDataConnectResponse response = myDataConnectService.connect(user.getUserId());
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PostMapping("/sync")
  public ResponseEntity<ApiResponse<Void>> sync(
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    myDataConnectService.sync(user.getUserId());
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
