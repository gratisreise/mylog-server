package com.mylog.domain.auth.dto.request;

import com.mylog.common.CommonValue;
import com.mylog.common.enums.OauthProvider;
import com.mylog.common.validation.Password;
import com.mylog.domain.member.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
