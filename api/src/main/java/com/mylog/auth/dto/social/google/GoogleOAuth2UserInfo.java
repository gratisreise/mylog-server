package com.mylog.auth.dto.social.google;


import com.mylog.auth.dto.social.OAuth2UserInfo;
import com.mylog.enums.OauthProvider;
import com.mylog.member.entity.Member;
import java.util.UUID;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GoogleOAuth2UserInfo implements OAuth2UserInfo {
    private final GoogleUserInfo googleUserInfo;

    @Override
    public String getId() {
        return googleUserInfo.id();
    }

    @Override
    public String getName() {
        return googleUserInfo.name();
    }


    @Override
    public String getImageUrl() {
        return googleUserInfo.picture();
    }

    @Override
    public Member toEntity(){
        return Member.builder()
            .provider(OauthProvider.GOOGLE)
            .providerId(getId())
            .memberName(getName())
            .nickname(getId() + OauthProvider.GOOGLE)
            .profileImg(getImageUrl())
            .build();
    }
}
