package com.mylog.model.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

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
