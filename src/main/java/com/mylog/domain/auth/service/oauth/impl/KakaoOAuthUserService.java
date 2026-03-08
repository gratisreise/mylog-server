package com.mylog.domain.auth.service.oauth.impl;

import com.mylog.common.annotations.OAuthServiceType;
import com.mylog.common.enums.OauthProvider;
import com.mylog.domain.auth.dto.request.OAuthRequest;
import com.mylog.domain.auth.dto.response.LoginResponse;
import com.mylog.domain.auth.service.TokenService;
import com.mylog.domain.auth.service.oauth.OAuthUserService;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.service.MemberWriter;
import com.mylog.external.oauth.kakao.KakaoApiClient;
import com.mylog.external.oauth.kakao.KakaoTokenResponse;
import com.mylog.external.oauth.kakao.KakaoUserInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@OAuthServiceType(OauthProvider.KAKAO)
public class KakaoOAuthUserService implements OAuthUserService {

  private final MemberWriter memberWriter;
  private final TokenService tokenService;
  private final KakaoApiClient apiClient;

  @Override
  public LoginResponse authenticate(OAuthRequest request) {
    // 인증 코드를 액세스 토큰으로 교환
    KakaoTokenResponse tokenResponse = apiClient.exchangeCodeForToken(request.code());

    // 제공업체에서 사용자 정보 조회
    KakaoUserInfoResponse userInfo = apiClient.getUserInfo(tokenResponse.accessToken());

    // 저장 및 수정
    Member member = memberWriter.saveOrUpdate(userInfo.toEntity());

    // 토큰반환
    return tokenService.generateToken(member.getId());
  }
}
