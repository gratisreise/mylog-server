<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/dto/request/RefreshRequest.java
package com.mylog.domain.auth.dto.request;

import com.mylog.common.enums.OauthProvider;
import com.mylog.enums.OauthProvider;
import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
    @NotBlank
    String refreshToken,

    @NotBlank
    OauthProvider provider
) {}
