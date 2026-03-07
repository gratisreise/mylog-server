package com.mylog.domain.auth.service.oauth;

import com.mylog.common.annotations.OAuth2ServiceType;
import com.mylog.common.enums.OauthProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OAuthUserServiceFactory {
    private final Map<OauthProvider, OAuthUserService> serviceMap = new HashMap<>();

    public OAuthUserServiceFactory(
        List<OAuthUserService> services) {
        for (OAuthUserService service : services) {
            OAuth2ServiceType type = service.getClass().getAnnotation(OAuth2ServiceType.class);
            if (type != null) {
                serviceMap.put(type.value(), service);
            }
        }
    }

    public OAuthUserService getOAuth2UserService(OauthProvider provider){
        return serviceMap.get(provider);
    }
}
