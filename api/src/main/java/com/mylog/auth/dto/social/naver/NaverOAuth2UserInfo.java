package com.mylog.auth.dto.social.naver;


import com.mylog.auth.dto.social.OAuth2UserInfo;
import com.mylog.enums.OauthProvider;
import com.mylog.member.entity.Member;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NaverOAuth2UserInfo implements OAuth2UserInfo {
    private final NaverUserInfo naverUserInfo;

    @Override
    public String getId() {
        return naverUserInfo.response().id();
    }

    @Override
    public String getName() {
        return naverUserInfo.response().name();
    }

    @Override
    public String getImageUrl() {
        return naverUserInfo.response().profileImage();
    }

    @Override
    public Member toEntity(){
        return Member.builder()
            .provider(OauthProvider.NAVER)
            .providerId(getId())
            .memberName(getName())
            .nickname(getId() + OauthProvider.NAVER)
            .profileImg(getImageUrl())
            .build();
    }
}
