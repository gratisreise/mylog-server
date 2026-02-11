package com.mylog.domain.auth.service.social.naver;


import com.mylog.common.annotations.OAuth2ServiceType;
import com.mylog.common.security.JwtUtil;
import com.mylog.common.enums.OauthProvider;
import com.mylog.common.exception.CMissingDataException;
import com.mylog.domain.auth.dto.social.OAuth2UserInfo;
import com.mylog.domain.auth.dto.social.OAuthRequest;
import com.mylog.domain.auth.dto.social.naver.NaverOAuth2UserInfo;
import com.mylog.domain.auth.dto.social.naver.NaverTokenResponse;
import com.mylog.domain.auth.dto.social.naver.NaverUserInfo;
import com.mylog.domain.member.Member;
import com.mylog.domain.member.repository.MemberRepository;
import com.mylog.domain.auth.service.RefreshTokenService;
import com.mylog.domain.category.service.CategoryWriter;
import com.mylog.domain.auth.service.social.AbstractOAuth2UserService;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@OAuth2ServiceType(OauthProvider.NAVER)
public class NaverOAuth2UserService extends AbstractOAuth2UserService {
    private final MemberRepository memberRepository;
    private final NaverTokenClient naverTokenClient;
    private final NaverUserClient naverUserClient;


    @Value("${oauth2.client.naver.client-id}")
    private String clientId;

    @Value("${oauth2.client.naver.client-secret}")
    private String clientSecret;

    @Value("${oauth2.client.naver.redirect-uri}")
    private String redirectUri;

    public NaverOAuth2UserService(
        JwtUtil jwtUtil, RefreshTokenService refreshTokenService,
        CategoryWriter categoryWriter,
        MemberRepository memberRepository,
        NaverTokenClient naverTokenClient, NaverUserClient naverUserClient) {
        super(jwtUtil, refreshTokenService, categoryWriter);
        this.memberRepository = memberRepository;
        this.naverTokenClient = naverTokenClient;
        this.naverUserClient = naverUserClient;
    }

    @Override
    public String getAccessToken(OAuthRequest request) {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("code", request.getCode());
        params.put("state", request.getState());
        params.put("redirect_uri", redirectUri);

        NaverTokenResponse response = naverTokenClient.getAccessToken(params);

        if (response == null || response.getAccessToken() == null) {
            throw new CMissingDataException("토큰 응답이 비어있습니다.");
        }

        return response.getAccessToken();
    }

    @Override
    public OAuth2UserInfo getUserInfo(String accessToken) {
        NaverUserInfo data = naverUserClient.getUserInfo("Bearer " + accessToken);

        if (data == null || data.response() == null) {
            throw new CMissingDataException("사용자 정보가 비어있습니다.");
        }

        return new NaverOAuth2UserInfo(data);
    }

    @Override
    public Member createOrUpdateMember(OAuth2UserInfo userInfo) {
        Member member = memberRepository.findByProviderAndProviderId(
            OauthProvider.NAVER,
            userInfo.getId())
            .orElseGet(Member::new);

        member.update(userInfo, OauthProvider.NAVER);

        return memberRepository.save(member);
    }
}
