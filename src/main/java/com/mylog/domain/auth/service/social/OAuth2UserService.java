<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/service/social/OAuth2UserService.java
package com.mylog.domain.auth.service.social;


import com.mylog.auth.dto.LoginResponse;
import com.mylog.auth.dto.social.OAuth2UserInfo;
import com.mylog.auth.dto.social.OAuthRequest;
import com.mylog.domain.auth.dto.response.LoginResponse;
import com.mylog.domain.auth.dto.social.OAuth2UserInfo;
import com.mylog.domain.auth.dto.social.OAuthRequest;
import com.mylog.domain.member.Member;
import com.mylog.member.entity.Member;
>>>>>>>> origin/main:api/src/main/java/com/mylog/auth/service/social/OAuth2UserService.java

public interface OAuth2UserService {
    LoginResponse login(OAuthRequest oAuthRequest);
    String getAccessToken(OAuthRequest oAuthRequest);
    OAuth2UserInfo getUserInfo(String accessToken);
    Member createOrUpdateMember(OAuth2UserInfo userInfo);
}
