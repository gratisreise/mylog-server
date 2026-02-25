package com.mylog.domain.auth.dto.request;

import com.mylog.common.validation.Password;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.crypto.password.PasswordEncoder;


public record LoginRequest (
    @Length(min = 8, max = 30) String email,
    @Password String password
){

}
