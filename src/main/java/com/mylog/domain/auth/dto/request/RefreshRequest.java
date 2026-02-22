<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/dto/request/RefreshRequest.java
package com.mylog.domain.auth.dto.request;

import com.mylog.common.enums.OauthProvider;
========
package com.mylog.auth.dto;


import com.mylog.enums.OauthProvider;
>>>>>>>> origin/main:api/src/main/java/com/mylog/auth/dto/RefreshRequest.java
import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
    @NotBlank
    String refreshToken,

    @NotBlank
    OauthProvider provider
) {}
