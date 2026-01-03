package com.mylog.member.dto;

import com.mylog.annotations.Password;
import com.mylog.member.entity.Member;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.crypto.password.PasswordEncoder;

public record MemberUpdateRequest(
    @Password
    String password,

    @Length(min = 3, max = 30)
    @NotBlank String memberName,

    @Length(min = 3, max = 30)
    @NotBlank String nickname,

    @Length(max = 200)
    @NotBlank String bio,

    String imageUrl
) {

    public Member toEntity(PasswordEncoder encoder, String imageUrl) {
        return Member.builder()
            .password(encoder.encode(password))
            .memberName(memberName)
            .nickname(nickname)
            .bio(bio)
            .profileImg(imageUrl)
            .build();
    }
}
