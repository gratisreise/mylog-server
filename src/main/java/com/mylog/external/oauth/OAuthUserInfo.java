package com.mylog.external.oauth;

import com.mylog.domain.member.entity.Member;

public interface OAuthUserInfo {

  String id();

  String name();

  String profileImage();

  Member toEntity();
}
