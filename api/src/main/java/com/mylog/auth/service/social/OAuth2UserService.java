package com.mylog.auth.service.social;


import com.mylog.auth.dto.LoginResponse;
import com.mylog.auth.dto.social.OAuth2UserInfo;
import com.mylog.auth.dto.social.OAuthRequest;
import com.mylog.member.entity.Member;

public interface OAuth2UserService {
    LoginResponse login(OAuthRequest oAuthRequest);
    String getAccessToken(OAuthRequest oAuthRequest);
    OAuth2UserInfo getUserInfo(String accessToken);
    Member createOrUpdateMember(OAuth2UserInfo userInfo);
}
