<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/dto/social/OAuthRequest.java
<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/dto/social/OAuthRequest.java
package com.mylog.domain.auth.dto.social;

import com.mylog.common.enums.OauthProvider;
========
package com.mylog.auth.dto.social;


import com.mylog.enums.OauthProvider;
>>>>>>>> origin/main:api/src/main/java/com/mylog/auth/dto/social/OAuthRequest.java
========
package com.mylog.auth.dto.social;


import com.mylog.enums.OauthProvider;
>>>>>>>> df0a55de6d27f9fdc5dd1d7257f9e30801976b60:api/src/main/java/com/mylog/auth/dto/social/OAuthRequest.java
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OAuthRequest {
    @NotBlank
    private String code;
    @NotNull
    private OauthProvider provider;
    @NotBlank
    private String state;
}

