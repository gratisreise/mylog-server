package com.mylog.model.dto.social.google;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleUserInfo(
    @JsonProperty("sub") String id,
    String email,
    String name,
    String picture
) {}
