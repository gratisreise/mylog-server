<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/dto/social/google/GoogleUserInfo.java
package com.mylog.domain.auth.dto.social.google;
========
package com.mylog.auth.dto.social.google;
>>>>>>>> origin/main:api/src/main/java/com/mylog/auth/dto/social/google/GoogleUserInfo.java

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleUserInfo(
    @JsonProperty("sub") String id,
    String email,
    String name,
    String picture
) {}
