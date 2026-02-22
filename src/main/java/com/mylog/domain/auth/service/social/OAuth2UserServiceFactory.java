<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/service/social/OAuth2UserServiceFactory.java
package com.mylog.domain.auth.service.social;

import com.mylog.common.annotations.OAuth2ServiceType;
import com.mylog.common.enums.OauthProvider;
========
package com.mylog.auth.service.social;


import com.mylog.annotations.OAuth2ServiceType;
import com.mylog.enums.OauthProvider;
>>>>>>>> origin/main:api/src/main/java/com/mylog/auth/service/social/OAuth2UserServiceFactory.java
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OAuth2UserServiceFactory {
    private final Map<OauthProvider, OAuth2UserService> serviceMap = new HashMap<>();

    public OAuth2UserServiceFactory(
        List<OAuth2UserService> services) {
        for (OAuth2UserService service : services) {
            OAuth2ServiceType type = service.getClass().getAnnotation(OAuth2ServiceType.class);
            if (type != null) {
                serviceMap.put(type.value(), service);
            }
        }
    }

    public OAuth2UserService getOAuth2UserService(OauthProvider provider){
        return serviceMap.get(provider);
    }
}