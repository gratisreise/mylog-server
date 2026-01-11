package com.mylog.auth.dto;

import org.hibernate.validator.constraints.Length;


public record LoginRequest (
    @Length(min = 8, max = 30) String email,
    @Length(min = 8, max = 20) String password
){ }
