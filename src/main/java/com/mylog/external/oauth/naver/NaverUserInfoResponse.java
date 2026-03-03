package com.mylog.external.oauth.naver;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.mylog.common.enums.OauthProvider;
import com.mylog.domain.member.entity.Member;
import com.mylog.external.oauth.OAuthUserInfo;

public record NaverUserInfoResponse(
        @JsonProperty("response")
        NaverProfile response
) implements OAuthUserInfo {

    @Override
    public String id() {
        return response != null ? response.id() : null;
    }

    @Override
    public String name() {
        return response != null ? response.nickname() : null;
    }

    @Override
    public String profileImage() {
        return response != null ? response.profileImage() : null;
    }

    @Override
    public Member toEntity() {
        return Member.builder()
            .email(this.id() + "@naver.com")
            .memberName(this.name() != null ? this.name() : "User")
            .profileImg(this.profileImage())
            .provider(OauthProvider.GOOGLE)
            .providerId(this.id())
            .build();
    }

    public record NaverProfile(
            String id,

            String nickname,

            @JsonProperty("profile_image")
            String profileImage,

            String name
    ) {
    }
}
