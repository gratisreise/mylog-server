package com.mylog.dto;

import lombok.Getter;

@Getter
public class UpdateMemberRequest {
    private String email;
    private String password;
    private String memberName;
    private String nickname;
    private String profileImage;
    private String bio;
}
