package com.mylog.external.oauth.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mylog.common.enums.OauthProvider;
import com.mylog.domain.member.Member;
import com.mylog.external.oauth.OAuthUserInfo;


public record GoogleUserInfoResponse(
        String id,

        String name,

        @JsonProperty("picture")
        String profileImage
) implements OAuthUserInfo {

        @Override
        public Member toEntity() {
                return Member.builder()
                    .email(this.id() + "@google.com")
                    .memberName(this.name() != null ? this.name() : "User")
                    .profileImg(this.profileImage())
                    .provider(OauthProvider.GOOGLE)
                    .providerId(this.id())
                    .build();
        }

}
