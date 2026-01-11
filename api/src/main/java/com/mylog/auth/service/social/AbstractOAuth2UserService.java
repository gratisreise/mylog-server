package com.mylog.auth.service.social;


import com.mylog.auth.dto.LoginResponse;
import com.mylog.auth.dto.social.OAuth2UserInfo;
import com.mylog.auth.dto.social.OAuthRequest;
import com.mylog.auth.service.RefreshTokenService;
import com.mylog.category.service.CategoryWriter;
import com.mylog.member.entity.Member;
import com.mylog.utils.JwtUtil;
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

//        categoryWriter.createCategory(member);

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
