package com.mylog.external.oauth.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mylog.common.enums.OauthProvider;
import com.mylog.domain.member.Member;
import com.mylog.external.oauth.OAuthUserInfo;

public record KakaoUserInfoResponse(
        String id,

        @JsonProperty("kakao_account")
        KakaoProfile kakaoAccount
) implements OAuthUserInfo {

    @Override
    public String name() {
        return kakaoAccount != null ? kakaoAccount.nickname() : null;
    }

    @Override
    public String profileImage() {
        return kakaoAccount != null ? kakaoAccount.profileImageUrl() : null;
    }

    @Override
    public Member toEntity() {
        return Member.builder()
            .email(this.id() + "@kakao.com")
            .memberName(this.name() != null ? this.name() : "User")
            .profileImg(this.profileImage())
            .provider(OauthProvider.GOOGLE)
            .providerId(this.id())
            .build();
    }


    public record KakaoProfile(
            @JsonProperty("profile_nickname")
            String nickname,

            @JsonProperty("profile_image_url")
            String profileImageUrl
    ) {
    }
}
