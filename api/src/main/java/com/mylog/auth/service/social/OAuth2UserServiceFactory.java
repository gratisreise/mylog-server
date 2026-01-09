package com.mylog.auth.service.social;


import com.mylog.annotations.OAuth2ServiceType;
import com.mylog.enums.OauthProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OAuth2UserServiceFactory {
    private final Map<OauthProvider, com.mylog.api.auth.service.social.OAuth2UserService> serviceMap = new HashMap<>();

    public OAuth2UserServiceFactory(
        List<com.mylog.api.auth.service.social.OAuth2UserService> services) {
        for (com.mylog.api.auth.service.social.OAuth2UserService service : services) {
            OAuth2ServiceType type = service.getClass().getAnnotation(OAuth2ServiceType.class);
            if (type != null) {
                serviceMap.put(type.value(), service);
            }
        }
    }

    public com.mylog.api.auth.service.social.OAuth2UserService getOAuth2UserService(OauthProvider provider){
        return serviceMap.get(provider);
    }
}