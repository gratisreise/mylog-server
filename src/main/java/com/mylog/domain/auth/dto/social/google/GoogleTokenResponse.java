<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/dto/social/google/GoogleTokenResponse.java
package com.mylog.domain.auth.dto.social.google;
========
package com.mylog.auth.dto.social.google;
>>>>>>>> origin/main:api/src/main/java/com/mylog/auth/dto/social/google/GoogleTokenResponse.java

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("expires_in")
    private Long expiresIn;
    @JsonProperty("scope")
    private String scope;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("id_token")
    private String idToken;
}
