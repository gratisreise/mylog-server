package com.mylog.auth.service.social.kakao;

import com.mylog.annotations.OAuth2ServiceType;
import com.mylog.auth.dto.social.OAuth2UserInfo;
import com.mylog.auth.dto.social.OAuthRequest;
import com.mylog.auth.dto.social.kako.KakaoOAuth2UserInfo;
import com.mylog.auth.dto.social.kako.KakaoTokenResponse;
import com.mylog.auth.dto.social.kako.KakaoUserInfo;
import com.mylog.auth.service.RefreshTokenService;
import com.mylog.auth.service.social.AbstractOAuth2UserService;
import com.mylog.auth.service.social.kakao.KakaoUserClient;
import com.mylog.category.service.CategoryWriter;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.common.CMissingDataException;
import com.mylog.exception.common.CommonError;
import com.mylog.member.entity.Member;
import com.mylog.member.repository.MemberRepository;
import com.mylog.member.service.MemberReader;
import com.mylog.member.service.MemberWriter;
import com.mylog.utils.JwtUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@OAuth2ServiceType(OauthProvider.KAKAO)
public class KakaoOAuth2UserService extends AbstractOAuth2UserService {

    private final MemberWriter memberWriter;
    private final MemberReader memberReader;
    private final KakaoTokenClient kakaoTokenClient;
    private final KakaoUserClient kakaoUserClient;

    @Value("${oauth2.client.kakao.client-id}")
    private String clientId;

    @Value("${oauth2.client.kakao.client-secret}")
    private String clientSecret;

    @Value("${oauth2.client.kakao.redirect-uri}")
    private String redirectUri;

    public KakaoOAuth2UserService(
        JwtUtil jwtUtil, RefreshTokenService refreshTokenService,
        CategoryWriter categoryWriter,
        MemberWriter memberWriter,
        MemberReader memberReader,
        KakaoTokenClient kakaoTokenClient,
        KakaoUserClient kakaoUserClient
    ){
        super(jwtUtil, refreshTokenService, categoryWriter);
        this.memberWriter = memberWriter;
        this.memberReader = memberReader;
        this.kakaoTokenClient = kakaoTokenClient;
        this.kakaoUserClient = kakaoUserClient;
    }

    @Override
    public String getAccessToken(OAuthRequest request) {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("client_id", clientId);
        params.put("redirect_uri", redirectUri);
        params.put("code", request.getCode());
        params.put("client_secret", clientSecret);

        KakaoTokenResponse response = kakaoTokenClient.getAccessToken(params);

        if (response == null || response.getAccessToken() == null) {
            throw new CMissingDataException(CommonError.TOKEN_IS_EMPTY);
        }

        return response.getAccessToken();
    }

    @Override
    public OAuth2UserInfo getUserInfo(String accessToken) {
        KakaoUserInfo userInfo = kakaoUserClient.getUserInfo(setBearerAuth(accessToken));

        if (userInfo == null) {
            throw new CMissingDataException(CommonError.USER_IS_EMPTY);
        }

        return new KakaoOAuth2UserInfo(userInfo);
    }

    @Override
    public Member createOrUpdateMember(OAuth2UserInfo userInfo) {
        Optional<Member> member = memberReader.findByProviderAndProviderId(
            OauthProvider.KAKAO,
            userInfo.getId()
        );

        if(member.isEmpty()){
            Member newMember = userInfo.toEntity();
            return memberWriter.saveMember(newMember);
        }

        return member.get();
    }
}
