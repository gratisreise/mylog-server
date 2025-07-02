package com.mylog.model.dto.member;

import jakarta.validation.constraints.NotBlank;
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

    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")
    @NotBlank
    private String password;

    @Length(min = 3, max = 30)
    @NotBlank
    private String memberName;

    @Length(min = 3, max = 30)
    @NotBlank
    private String nickname;

    @Length(max = 200)
    @NotBlank
    private String bio;


}
