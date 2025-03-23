package com.mylog.service.social;

import com.mylog.config.JwtUtil;
import com.mylog.dto.LoginResponse;
import com.mylog.dto.social.OAuthRequest;
import com.mylog.entity.Member;
import com.mylog.interfaces.OAuth2UserInfo;
import com.mylog.interfaces.OAuth2UserService;
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

        String refreshToken = jwtUtil.createRefreshToken(member.getId());
        refreshTokenService.saveRefreshToken(member.getEmail(), refreshToken);

        String jwtAccessToken = jwtUtil.createAccessToken(member.getId());
        return new LoginResponse(jwtAccessToken, refreshToken);
    }
}
