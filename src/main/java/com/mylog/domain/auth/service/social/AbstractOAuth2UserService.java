<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/service/social/AbstractOAuth2UserService.java
package com.mylog.domain.auth.service.social;

import com.mylog.common.security.JwtProvider;
import com.mylog.model.dto.auth.LoginResponse;
import com.mylog.domain.auth.dto.social.OAuth2UserInfo;
import com.mylog.domain.auth.dto.social.OAuthRequest;
import com.mylog.domain.member.Member;
import com.mylog.domain.auth.service.RefreshTokenService;
import com.mylog.domain.category.service.CategoryWriter;
========
package com.mylog.auth.service.social;


import com.mylog.auth.dto.LoginResponse;
import com.mylog.auth.dto.social.OAuth2UserInfo;
import com.mylog.auth.dto.social.OAuthRequest;
import com.mylog.auth.service.RefreshTokenService;
import com.mylog.category.service.CategoryWriter;
import com.mylog.member.entity.Member;
import com.mylog.utils.JwtUtil;
>>>>>>>> origin/main:api/src/main/java/com/mylog/auth/service/social/AbstractOAuth2UserService.java
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/service/social/AbstractOAuth2UserService.java
public abstract class AbstractOAuth2UserService implements OAuth2UserService {
    protected final JwtProvider jwtProvider;
========
public abstract class AbstractOAuth2UserService implements
    OAuth2UserService {
    protected final JwtUtil jwtUtil;
>>>>>>>> origin/main:api/src/main/java/com/mylog/auth/service/social/AbstractOAuth2UserService.java
    protected final RefreshTokenService refreshTokenService;
    private final CategoryWriter categoryWriter;

    @Override
    public LoginResponse login(OAuthRequest request){
        String accessToken = getAccessToken(request);
        OAuth2UserInfo userInfo = getUserInfo(accessToken);
        Member member = createOrUpdateMember(userInfo);

        long memberId = member.getId();
        String username = String.valueOf(memberId);

        categoryWriter.createCategory(member);

        String refreshToken = jwtProvider.createRefreshToken(username);
        refreshTokenService.saveRefreshToken(username, refreshToken);

<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/service/social/AbstractOAuth2UserService.java
        String jwtAccessToken = jwtProvider.createAccessToken(username, member.getId());
        log.info("access: {}", jwtAccessToken);
        log.info("refresh: {}", refreshToken);
        log.info("소셜로그인 성공");
========
        String jwtAccessToken = jwtUtil.createAccessToken(username, member.getId());
>>>>>>>> origin/main:api/src/main/java/com/mylog/auth/service/social/AbstractOAuth2UserService.java
        return new LoginResponse(jwtAccessToken, refreshToken);
    }

    protected String setBearerAuth(String accessToken){
        return "Bearer " + accessToken;
    }
}
