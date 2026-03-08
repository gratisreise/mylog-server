package com.mylog.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {
  private String accessToken;
  private String refreshToken;

  public static LoginResponse of(String accessToken, String refreshToken) {
    return LoginResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
  }
}
