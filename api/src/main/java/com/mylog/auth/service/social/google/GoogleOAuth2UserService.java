package com.mylog.auth.service.social.google;


import com.mylog.annotations.OAuth2ServiceType;
import com.mylog.auth.dto.social.OAuth2UserInfo;
import com.mylog.auth.dto.social.OAuthRequest;
import com.mylog.auth.dto.social.google.GoogleOAuth2UserInfo;
import com.mylog.auth.dto.social.google.GoogleTokenResponse;
import com.mylog.auth.dto.social.google.GoogleUserInfo;
import com.mylog.auth.service.RefreshTokenService;
import com.mylog.auth.service.social.AbstractOAuth2UserService;
import com.mylog.category.service.CategoryWriter;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.common.CMissingDataException;
import com.mylog.exception.common.CommonError;
import com.mylog.member.entity.Member;
import com.mylog.member.service.MemberReader;
import com.mylog.member.service.MemberWriter;
import com.mylog.utils.JwtUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@OAuth2ServiceType(OauthProvider.GOOGLE)
public class GoogleOAuth2UserService extends AbstractOAuth2UserService {
    @Value("${oauth2.client.google.client-id}")
    private String clientId;

    @Value("${oauth2.client.google.client-secret}")
    private String clientSecret;

    @Value("${oauth2.client.google.redirect-uri}")
    private String redirectUri;

    private final MemberWriter memberWriter;
    private final MemberReader memberReader;
    private final GoogleTokenClient googleTokenClient;
    private final GoogleUserClient googleUserClient;

    public GoogleOAuth2UserService(
        JwtUtil jwtUtil, RefreshTokenService refreshTokenService,
        CategoryWriter categoryWriter,
        MemberWriter memberWriter,
        MemberReader memberReader,
        GoogleTokenClient googleTokenClient,
        GoogleUserClient googleUserClient
    ) {
        super(jwtUtil, refreshTokenService, categoryWriter);
        this.memberWriter = memberWriter;
        this.memberReader = memberReader;
        this.googleTokenClient = googleTokenClient;
        this.googleUserClient = googleUserClient;
    }

    @Override
    public String getAccessToken(OAuthRequest request) {

        Map<String, String> params = new HashMap<>();
        params.put("code", request.getCode());
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("redirect_uri", redirectUri);
        params.put("grant_type", "authorization_code");

        GoogleTokenResponse response = googleTokenClient.getAccessToken(params);

        if(response == null){
            throw new CMissingDataException(CommonError.TOKEN_IS_EMPTY);
        }

        return response.getAccessToken();
    }

    @Override
    public OAuth2UserInfo getUserInfo(String accessToken) {
        GoogleUserInfo userInfo = googleUserClient.getUserInfo(setBearerAuth(accessToken));
        log.info("googleUserInfo: {}", userInfo);
        if(userInfo == null){
            throw new CMissingDataException(CommonError.USER_IS_EMPTY);
        }
        return new GoogleOAuth2UserInfo(userInfo);
    }

    @Override
    public Member createOrUpdateMember(OAuth2UserInfo userInfo) {
        Optional<Member> member = memberReader.findByProviderAndProviderId(
            OauthProvider.GOOGLE,
            userInfo.getId()
        );

        if(member.isEmpty()){
            Member newMember = userInfo.toEntity();
            return memberWriter.saveMember(newMember);
        }
        return member.get();
    }
}
