<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/dto/social/naver/NaverResponse.java
package com.mylog.domain.auth.dto.social.naver;
========
package com.mylog.auth.dto.social.naver;
>>>>>>>> origin/main:api/src/main/java/com/mylog/auth/dto/social/naver/NaverResponse.java

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverResponse(
    String id, String nickname, String name,
    @JsonProperty("profile_image") String profileImage
) {}
