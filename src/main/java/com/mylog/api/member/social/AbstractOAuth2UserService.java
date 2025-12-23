package com.mylog.api.member.social;

import com.mylog.config.JwtUtil;
import com.mylog.model.dto.auth.LoginResponse;
import com.mylog.api.auth.social.OAuth2UserInfo;
import com.mylog.api.auth.social.OAuthRequest;
import com.mylog.domain.entity.Member;
import com.mylog.api.auth.RefreshTokenService;
import com.mylog.api.category.CategoryWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractOAuth2UserService implements OAuth2UserService {
    protected final JwtUtil jwtUtil;
    protected final RefreshTokenService refreshTokenService;
    private final CategoryWriter categoryWriter;

    @Override
    public LoginResponse login(OAuthRequest request){
        String accessToken = getAccessToken(request);
//        log.info("{}", accessToken);
        OAuth2UserInfo userInfo = getUserInfo(accessToken);
//        log.info("{}", userInfo);
        Member member = createOrUpdateMember(userInfo);
//        log.info("{}", member);

        long memberId = member.getId();
        String username = String.valueOf(memberId);

        categoryWriter.createCategory(member);

        String refreshToken = jwtUtil.createRefreshToken(username);
        refreshTokenService.saveRefreshToken(username, refreshToken);

        String jwtAccessToken = jwtUtil.createAccessToken(username, member.getId());
        log.info("access: {}", jwtAccessToken);
        log.info("refresh: {}", refreshToken);
        log.info("소셜로그인 성공");
        return new LoginResponse(jwtAccessToken, refreshToken);
    }

    protected String setBearerAuth(String accessToken){
        return "Bearer " + accessToken;
    }
}
