package com.mylog.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RefreshResponse {
  private String accessToken;
  private String refreshToken;

  public static RefreshResponse of(String accessToken, String refreshToken) {
    return RefreshResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
  }
}
