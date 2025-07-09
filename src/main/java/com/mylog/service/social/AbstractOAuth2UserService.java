package com.mylog.service.social;

import com.mylog.config.JwtUtil;
import com.mylog.model.dto.auth.LoginResponse;
import com.mylog.model.dto.social.OAuth2UserInfo;
import com.mylog.model.dto.social.OAuthRequest;
import com.mylog.model.entity.Member;
import com.mylog.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractOAuth2UserService implements OAuth2UserService {
    protected final JwtUtil jwtUtil;
    protected final RefreshTokenService refreshTokenService;

    @Override
    public LoginResponse login(OAuthRequest request){
        String accessToken = getAccessToken(request);
        log.info("{}", accessToken);
        OAuth2UserInfo userInfo = getUserInfo(accessToken);
        log.info("{}", userInfo);
        Member member = createOrUpdateMember(userInfo);
        log.info("{}", member);

        String refreshToken = jwtUtil.createRefreshToken(member.getNickname());
        refreshTokenService.saveRefreshToken(member.getNickname(), refreshToken);

        String jwtAccessToken = jwtUtil.createAccessToken(member.getNickname(), member.getId());
        return new LoginResponse(jwtAccessToken, refreshToken);
    }

    protected String setBearerAuth(String accessToken){
        return "Bearer " + accessToken;
    }
}
