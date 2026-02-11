package com.mylog.domain.auth.dto.social.kako;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Properties(
    @JsonProperty("profile_image") String profileImage
){ }
