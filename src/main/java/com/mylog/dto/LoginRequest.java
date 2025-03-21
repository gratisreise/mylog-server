package com.mylog.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@AllArgsConstructor
public class LoginRequest {
    @Length(min = 8, max = 30)
    private String email;

    @Length(min = 8, max = 20)
    private String password;

}
