package com.mylog.auth.dto.social.kako;


import com.mylog.auth.dto.social.OAuth2UserInfo;
import com.mylog.enums.OauthProvider;
import com.mylog.member.entity.Member;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class KakaoOAuth2UserInfo implements OAuth2UserInfo {
    private final KakaoUserInfo kakaoUserInfo;

    @Override
    public String getId() {
        return kakaoUserInfo.id().toString();
    }

    @Override
    public String getName() {
        return "KAKAO"+kakaoUserInfo.id();
    }

    @Override
    public String getImageUrl() {
        return kakaoUserInfo.properties().profileImage();
    }

    @Override
    public Member toEntity(){
        return Member.builder()
            .provider(OauthProvider.KAKAO)
            .providerId(getId())
            .memberName(getName())
            .nickname(getId() + OauthProvider.KAKAO)
            .profileImg(getImageUrl())
            .build();
    }
}
