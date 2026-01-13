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
public abstract class AbstractOAuth2UserService implements
    OAuth2UserService {
    protected final JwtUtil jwtUtil;
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

        String refreshToken = jwtUtil.createRefreshToken(username);
        refreshTokenService.saveRefreshToken(username, refreshToken);

        String jwtAccessToken = jwtUtil.createAccessToken(username, member.getId());
        return new LoginResponse(jwtAccessToken, refreshToken);
    }

    protected String setBearerAuth(String accessToken){
        return "Bearer " + accessToken;
    }
}
