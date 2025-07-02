package com.mylog.model.dto.social;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NaverUserInfo {
    private String id;
    private String nickname;
    private String name;
    @JsonProperty("profile_image")
    private String profileImage;
}
