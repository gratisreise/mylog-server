package com.mylog.domain.auth.service.oauth.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.mylog.common.enums.OauthProvider;
import com.mylog.domain.auth.dto.request.OAuthRequest;
import com.mylog.domain.auth.dto.response.LoginResponse;
import com.mylog.domain.auth.service.TokenService;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.service.MemberWriter;
import com.mylog.external.oauth.google.GoogleApiClient;
import com.mylog.external.oauth.google.GoogleTokenResponse;
import com.mylog.external.oauth.google.GoogleUserInfoResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoogleOAuthServiceTest {

  private static final String AUTH_CODE = "google-auth-code";
  private static final String GOOGLE_ACCESS_TOKEN = "google-access-token";
  private static final String ACCESS_TOKEN = "accessToken";
  private static final String REFRESH_TOKEN = "refreshToken";
  private static final Long MEMBER_ID = 1L;

  @Mock private MemberWriter memberWriter;
  @Mock private TokenService tokenService;
  @Mock private GoogleApiClient apiClient;

  @InjectMocks private GoogleOAuthService googleOAuthService;

  @Nested
  @DisplayName("authenticate")
  class Authenticate {

    @Test
    @DisplayName("인증 코드로 소셜 로그인에 성공한다")
    void 인증_코드로_소셜_로그인에_성공한다() {
      // given
      OAuthRequest request = new OAuthRequest(OauthProvider.GOOGLE, AUTH_CODE);
      GoogleTokenResponse tokenResponse =
          new GoogleTokenResponse(
              GOOGLE_ACCESS_TOKEN, "Bearer", 3600L, "rt", "scope", "idToken");
      GoogleUserInfoResponse userInfo =
          new GoogleUserInfoResponse("12345", "홍길동", "https://img.url");
      Member member = createMember();
      LoginResponse expected = LoginResponse.of(ACCESS_TOKEN, REFRESH_TOKEN);

      given(apiClient.exchangeCodeForToken(AUTH_CODE)).willReturn(tokenResponse);
      given(apiClient.getUserInfo(GOOGLE_ACCESS_TOKEN)).willReturn(userInfo);
      given(memberWriter.saveOrUpdate(any(Member.class))).willReturn(member);
      given(tokenService.generateToken(MEMBER_ID)).willReturn(expected);

      // when
      LoginResponse response = googleOAuthService.authenticate(request);

      // then
      assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
      assertThat(response.getRefreshToken()).isEqualTo(REFRESH_TOKEN);

      then(apiClient).should().exchangeCodeForToken(AUTH_CODE);
      then(apiClient).should().getUserInfo(GOOGLE_ACCESS_TOKEN);
      then(memberWriter).should().saveOrUpdate(any(Member.class));
      then(tokenService).should().generateToken(MEMBER_ID);
    }
  }

  private Member createMember() {
    return Member.builder()
        .id(MEMBER_ID)
        .email("12345@google.com")
        .memberName("홍길동")
        .build();
  }
}