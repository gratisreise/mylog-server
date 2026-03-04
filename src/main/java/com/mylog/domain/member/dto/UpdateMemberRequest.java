package com.mylog.domain.member.dto;

import com.mylog.common.validation.Password;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record UpdateMemberRequest(
    @Password String password,
    @Length(min = 3, max = 30) @NotBlank String memberName,
    @Length(min = 3, max = 30) @NotBlank String nickname,
    @Length(max = 200) @NotBlank String bio,
    String imageUrl) {}
