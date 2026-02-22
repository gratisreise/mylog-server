<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/dto/social/naver/NaverTokenResponse.java
<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/dto/social/naver/NaverTokenResponse.java
package com.mylog.domain.auth.dto.social.naver;
========
package com.mylog.auth.dto.social.naver;
>>>>>>>> origin/main:api/src/main/java/com/mylog/auth/dto/social/naver/NaverTokenResponse.java
========
package com.mylog.auth.dto.social.naver;
>>>>>>>> df0a55de6d27f9fdc5dd1d7257f9e30801976b60:api/src/main/java/com/mylog/auth/dto/social/naver/NaverTokenResponse.java

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NaverTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("token_type")
    private String tokenType;
}
