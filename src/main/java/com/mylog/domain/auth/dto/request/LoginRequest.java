package com.mylog.domain.auth.dto.request;

import com.mylog.common.annotations.Password;
import org.hibernate.validator.constraints.Length;

public record LoginRequest(@Length(min = 8, max = 30) String email, @Password String password) {}
