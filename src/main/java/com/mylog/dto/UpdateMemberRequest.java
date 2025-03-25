package com.mylog.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateMemberRequest {
    private String email;
    private String password;
    private String memberName;
    private String nickname;
    private String profileImage;
    private String bio;
}
