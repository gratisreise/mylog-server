package com.mylog.domain.auth.service.social;


import com.mylog.domain.auth.dto.response.LoginResponse;
import com.mylog.domain.auth.dto.social.OAuth2UserInfo;
import com.mylog.domain.auth.dto.social.OAuthRequest;
import com.mylog.domain.member.Member;

public interface OAuth2UserService {
    LoginResponse login(OAuthRequest oAuthRequest);
    String getAccessToken(OAuthRequest oAuthRequest);
    OAuth2UserInfo getUserInfo(String accessToken);
    Member createOrUpdateMember(OAuth2UserInfo userInfo);
}
