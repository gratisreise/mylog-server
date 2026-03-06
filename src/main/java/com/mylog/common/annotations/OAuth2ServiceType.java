package com.mylog.common.annotations;

<<<<<<<< HEAD:src/main/java/com/mylog/common/annotations/OAuth2ServiceType.java
import com.mylog.common.enums.OauthProvider;
========

import com.mylog.enums.OauthProvider;
>>>>>>>> origin/main:api/src/main/java/com/mylog/annotations/OAuth2ServiceType.java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface OAuth2ServiceType {
    OauthProvider value();
}