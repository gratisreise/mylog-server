package com.mylog.domain.auth.service.oauth;

import com.mylog.common.annotations.OAuthServiceType;
import com.mylog.common.enums.OauthProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OAuthUserServiceFactory {
  private final Map<OauthProvider, OAuthUserService> serviceMap = new HashMap<>();

  public OAuthUserServiceFactory(List<OAuthUserService> services) {
    for (OAuthUserService service : services) {
      OAuthServiceType type = service.getClass().getAnnotation(OAuthServiceType.class);
      if (type != null) {
        serviceMap.put(type.value(), service);
      }
    }
  }

  public OAuthUserService getOAuthUserService(OauthProvider provider) {
    return serviceMap.get(provider);
  }
}
