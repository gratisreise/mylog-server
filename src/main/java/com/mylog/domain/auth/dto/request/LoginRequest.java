<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/dto/request/LoginRequest.java
package com.mylog.domain.auth.dto.request;
========
package com.mylog.auth.dto;
>>>>>>>> origin/main:api/src/main/java/com/mylog/auth/dto/LoginRequest.java

import org.hibernate.validator.constraints.Length;


public record LoginRequest (
    @Length(min = 8, max = 30) String email,
    @Length(min = 8, max = 20) String password
){ }
