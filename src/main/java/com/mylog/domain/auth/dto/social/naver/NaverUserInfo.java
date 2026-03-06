<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/dto/social/naver/NaverUserInfo.java
package com.mylog.domain.auth.dto.social.naver;
========
package com.mylog.auth.dto.social.naver;
>>>>>>>> origin/main:api/src/main/java/com/mylog/auth/dto/social/naver/NaverUserInfo.java

public record NaverUserInfo(
    String resultcode,
    String message,
    NaverResponse response
) { }
