package com.symphony.bdk.http.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.symphony.bdk.http.api.util.TypeReference;

import org.junit.jupiter.api.Test;

class HttpClientTest {

  @Test
  public void usage() throws ApiException {

    final HttpClient httpClient = HttpClient.builder(this::mockedApiClientBuilder)
      .basePath("https://localhost:8080")
      .header("Connection", "Keep-Alive")
      .header("Keep-Alive", "timeout=5, max=1000")
      .cookie("foo", "bar")
      .build();

    final String response = httpClient.path("/api/v1/users")
        .header("Authorization", "Bearer AbCdEf123456")
        .get(new TypeReference<String>() {});
  }

  private ApiClientBuilder mockedApiClientBuilder() {
    final ApiClientBuilder apiClientBuilder = mock(ApiClientBuilder.class);
    when(apiClientBuilder.build()).thenReturn(mock(ApiClient.class));
    return apiClientBuilder;
  }
}
