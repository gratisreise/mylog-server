<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/dto/social/kako/Properties.java
package com.mylog.domain.auth.dto.social.kako;
========
package com.mylog.auth.dto.social.kako;
>>>>>>>> origin/main:api/src/main/java/com/mylog/auth/dto/social/kako/Properties.java

import com.fasterxml.jackson.annotation.JsonProperty;

public record Properties(
    @JsonProperty("profile_image") String profileImage
){ }
