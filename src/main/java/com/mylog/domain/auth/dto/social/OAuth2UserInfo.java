<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/dto/social/OAuth2UserInfo.java
package com.mylog.domain.auth.dto.social;
========
package com.mylog.auth.dto.social;

import com.mylog.member.entity.Member;
>>>>>>>> origin/main:api/src/main/java/com/mylog/auth/dto/social/OAuth2UserInfo.java

public interface OAuth2UserInfo {
    String getId();
    String getName();
    String getImageUrl();
    Member toEntity();
}
