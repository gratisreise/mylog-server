package com.mylog.dto.social;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class KakaoProperties {
    @JsonProperty("profile_image")
    private String profileImage;
}
