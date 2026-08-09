package org.jejuro.miraero.domain.mydata.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataTokenResponse;
import org.jejuro.miraero.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class MyDataAuthClientTest {

  private static final String BASE_URL = "http://localhost:9000";

  private RestTemplate restTemplate;
  private MockRestServiceServer mockServer;
  private MyDataAuthClient myDataAuthClient;

  private static final String CLIENT_SECRET = "test-client-secret";

  @BeforeEach
  void setUp() {
    restTemplate = new RestTemplate();
    mockServer = MockRestServiceServer.createServer(restTemplate);
    myDataAuthClient = new MyDataAuthClient(
        restTemplate,
        new ObjectMapper(),
        BASE_URL,
        "/mock/oauth/authorize",
        "/mock/oauth/token",
        CLIENT_SECRET
    );
  }

  @Test
  @DisplayName("인증코드를 요청할 때 X-Client-Secret 헤더를 포함해 코드 문자열을 반환한다")
  void requestAuthorizationCode() {
    mockServer.expect(requestTo(BASE_URL + "/mock/oauth/authorize"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("X-Client-Secret", CLIENT_SECRET))
        .andRespond(withSuccess(
            "{\"authorizationCode\":\"code-1\",\"expiresIn\":300}",
            MediaType.APPLICATION_JSON));

    assertEquals("code-1", myDataAuthClient.requestAuthorizationCode("miraero01@test.com"));
    mockServer.verify();
  }

  @Test
  @DisplayName("토큰 교환 시에도 X-Client-Secret 헤더를 포함한다")
  void exchangeToken() {
    mockServer.expect(requestTo(BASE_URL + "/mock/oauth/token"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("X-Client-Secret", CLIENT_SECRET))
        .andRespond(withSuccess(
            "{\"accessToken\":\"token-1\",\"expiresIn\":3600,\"kbUserId\":10001}",
            MediaType.APPLICATION_JSON));

    MyDataTokenResponse response = myDataAuthClient.exchangeToken("code-1");

    assertEquals("token-1", response.getAccessToken());
    assertEquals(3600L, response.getExpiresIn());
    assertEquals(10001L, response.getKbUserId());
    mockServer.verify();
  }

  @Test
  @DisplayName("목서버 오류 시 BusinessException을 던진다")
  void requestAuthorizationCode_serverError() {
    mockServer.expect(requestTo(BASE_URL + "/mock/oauth/authorize"))
        .andRespond(withServerError());

    assertThrows(BusinessException.class,
        () -> myDataAuthClient.requestAuthorizationCode("miraero01@test.com"));
  }
}
