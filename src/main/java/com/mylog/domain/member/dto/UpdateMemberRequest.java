package com.mylog.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record UpdateMemberRequest(
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")
    String password,

    @Length(min = 3, max = 30)
    @NotBlank String memberName,

    @Length(min = 3, max = 30)
    @NotBlank String nickname,

    @Length(max = 200)
    @NotBlank String bio,

    String imageUrl
) { }
