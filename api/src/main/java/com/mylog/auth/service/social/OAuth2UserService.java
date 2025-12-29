package com.mylog.api.auth.service.social;


import com.mylog.api.auth.dto.LoginResponse;
import com.mylog.api.auth.dto.social.OAuth2UserInfo;
import com.mylog.api.auth.dto.social.OAuthRequest;
import com.mylog.api.member.entity.Member;

public interface OAuth2UserService {
    LoginResponse login(OAuthRequest oAuthRequest);
    String getAccessToken(OAuthRequest oAuthRequest);
    OAuth2UserInfo getUserInfo(String accessToken);
    Member createOrUpdateMember(OAuth2UserInfo userInfo);
}
