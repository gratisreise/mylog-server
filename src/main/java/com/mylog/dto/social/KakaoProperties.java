package com.mylog.dto.social;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoProperties {
    @JsonProperty("profile_image")
    private String profileImage;
}
