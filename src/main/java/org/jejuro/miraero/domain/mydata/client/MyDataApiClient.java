package org.jejuro.miraero.domain.mydata.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataAccountResponse;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataTransactionResponse;
import org.jejuro.miraero.domain.mydata.exception.MyDataErrorCode;
import org.jejuro.miraero.global.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class MyDataApiClient {

  private static final String BEARER_PREFIX = "Bearer ";

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final String baseUrl;
  private final String accountsPath;
  private final String transactionsPath;

  @Autowired
  public MyDataApiClient(
      @Value("${mydata.api.base-url}") String baseUrl,
      @Value("${mydata.api.accounts-path}") String accountsPath,
      @Value("${mydata.api.transactions-path}") String transactionsPath
  ) {
    this(new RestTemplate(), new ObjectMapper(), baseUrl, accountsPath, transactionsPath);
  }

  MyDataApiClient(
      RestTemplate restTemplate,
      ObjectMapper objectMapper,
      String baseUrl,
      String accountsPath,
      String transactionsPath
  ) {
    this.restTemplate = restTemplate;
    // LocalDate/LocalDateTime fields in the response DTOs need this to deserialize.
    this.objectMapper = objectMapper.registerModule(new JavaTimeModule());
    this.baseUrl = baseUrl;
    this.accountsPath = accountsPath;
    this.transactionsPath = transactionsPath;
  }

  public List<MyDataAccountResponse> getAccounts(Long kbUserId, String accessToken) {
    return get(accountsPath, kbUserId, accessToken, new TypeReference<List<MyDataAccountResponse>>() {
    });
  }

  public List<MyDataTransactionResponse> getTransactions(Long kbUserId, String accessToken) {
    return get(transactionsPath, kbUserId, accessToken,
        new TypeReference<List<MyDataTransactionResponse>>() {
        });
  }

  private <T> T get(String path, Long kbUserId, String accessToken, TypeReference<T> typeReference) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken);

    try {
      ResponseEntity<String> response = restTemplate.exchange(
          baseUrl + path + "/" + kbUserId,
          HttpMethod.GET,
          new HttpEntity<>(headers),
          String.class
      );

      String responseBody = response.getBody();
      if (responseBody == null) {
        throw new BusinessException(MyDataErrorCode.MYDATA_SYNC_FAILED);
      }
      return objectMapper.readValue(responseBody, typeReference);
    } catch (RestClientException | JsonProcessingException exception) {
      throw new BusinessException(MyDataErrorCode.MYDATA_SYNC_FAILED);
    }
  }
}
