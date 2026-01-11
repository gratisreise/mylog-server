package com.mylog.auth.dto;


import com.mylog.annotations.Password;
import com.mylog.enums.OauthProvider;
import com.mylog.member.entity.Member;
import com.mylog.response.CommonValue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.crypto.password.PasswordEncoder;

public record SignUpRequest (
    @Email @NotBlank
    String email,

    @Length(min = 3, max = 30) @NotBlank
    String memberName,

    @Password @NotBlank
    String password
){

    public Member toEntity(PasswordEncoder encoder) {
        return Member.builder()
            .email(email)
            .memberName(memberName)
            .nickname(email)
            .providerId(email + OauthProvider.LOCAL)
            .provider(OauthProvider.LOCAL)
            .profileImg(CommonValue.BASIC_MEMBER_IMAGE)
            .password(encoder.encode(password))
            .build();
    }
}
