package com.mylog.service.social;

import com.mylog.config.JwtUtil;
import com.mylog.dto.LoginResponse;
import com.mylog.dto.social.OAuth2UserInfo;
import com.mylog.dto.social.OAuthRequest;
import com.mylog.entity.Member;
import com.mylog.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public abstract class AbstractOAuth2UserService implements OAuth2UserService {
    protected final RestTemplate restTemplate;
    protected final JwtUtil jwtUtil;
    protected final RefreshTokenService refreshTokenService;

    @Override
    public LoginResponse login(OAuthRequest request){
        String accessToken = getAccessToken(request);
        OAuth2UserInfo userInfo = getUserInfo(accessToken);
        Member member = createOrUpdateMember(userInfo);

        String refreshToken = jwtUtil.createRefreshToken(member.getNickname());
        refreshTokenService.saveRefreshToken(member.getNickname(), refreshToken);

        String jwtAccessToken = jwtUtil.createAccessToken(member.getNickname(), member.getId());
        return new LoginResponse(jwtAccessToken, refreshToken);
    }
}
