package com.mylog.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UpdateMemberRequest {
    @Length(min = 8, max = 30)
    private String email;

    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")
    private String password;

    @Length(min = 3, max = 30)
    private String memberName;

    @Length(min = 3, max = 30)
    private String nickname;

    private String profileImage;

    @Length(max = 200)
    private String bio;
}
