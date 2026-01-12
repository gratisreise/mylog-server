package com.mylog.auth.dto.social;

import com.mylog.member.entity.Member;

public interface OAuth2UserInfo {
    String getId();
    String getName();
    String getImageUrl();
    Member toEntity();
}
