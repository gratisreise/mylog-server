package com.mylog.api.auth.dto;

import com.mylog.common.enums.OauthProvider;
import com.mylog.member.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.crypto.password.PasswordEncoder;

public record SignUpRequest (
    @Email @NotBlank String email,
    @Length(min = 3, max = 30) @NotBlank String memberName,
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")
    @NotBlank
    String password
){

    public Member toEntity(PasswordEncoder encoder, String basicImageUrl) {
        return Member.builder()
            .email(email)
            .memberName(memberName)
            .nickname(email)
            .providerId(email + OauthProvider.LOCAL)
            .provider(OauthProvider.LOCAL)
            .profileImg(basicImageUrl)
            .password(encoder.encode(password))
            .build();
    }
}
