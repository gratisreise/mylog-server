<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/dto/social/kako/KakaoTokenResponse.java
<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/dto/social/kako/KakaoTokenResponse.java
package com.mylog.domain.auth.dto.social.kako;
========
package com.mylog.auth.dto.social.kako;
>>>>>>>> origin/main:api/src/main/java/com/mylog/auth/dto/social/kako/KakaoTokenResponse.java
========
package com.mylog.auth.dto.social.kako;
>>>>>>>> df0a55de6d27f9fdc5dd1d7257f9e30801976b60:api/src/main/java/com/mylog/auth/dto/social/kako/KakaoTokenResponse.java

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("expires_in")
    private Long expiresIn;
    @JsonProperty("scope")
    private String scope;
    @JsonProperty("refresh_token_expires_in")
    private Long refreshTokenExpiresIn;
}
