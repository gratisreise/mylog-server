package com.mylog.auth.dto.social.naver;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverResponse(
    String id, String nickname, String name,
    @JsonProperty("profile_image") String profileImage
) {}
