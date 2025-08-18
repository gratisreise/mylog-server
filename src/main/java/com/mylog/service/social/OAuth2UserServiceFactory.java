package com.mylog.service.social;

import com.mylog.enums.OauthProvider;
import com.mylog.exception.CInvalidDataException;
import com.mylog.service.social.google.GoogleOAuth2UserService;
import com.mylog.service.social.kakao.KakaoOAuth2UserService;
import com.mylog.service.social.naver.NaverOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2UserServiceFactory {
    private final GoogleOAuth2UserService googleOAuth2UserService;
    private final NaverOAuth2UserService naverOAuth2UserService;
    private final KakaoOAuth2UserService kakaoOAuth2UserService;

    public OAuth2UserService getOAuth2UserService(OauthProvider provider){
        switch(provider){
            case GOOGLE:
                return googleOAuth2UserService;
            case NAVER:
                return naverOAuth2UserService;
            case KAKAO:
                return kakaoOAuth2UserService;
            default:
                throw new CInvalidDataException("지원하지 않는 OAuth 제공자입니다." + provider);
        }
    }
}
