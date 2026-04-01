package com.mylog.domain.auth.service.oauth;

import com.mylog.common.annotations.OAuthServiceType;
import com.mylog.common.enums.OauthProvider;
import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OAuthServiceFactory {
  private final Map<OauthProvider, OAuthService> serviceMap = new HashMap<>();

  public OAuthServiceFactory(List<OAuthService> services) {
    for (OAuthService service : services) {
      OAuthServiceType type = service.getClass().getAnnotation(OAuthServiceType.class);
      if (type != null) {
        serviceMap.put(type.value(), service);
      }
    }
  }

  public OAuthService getOAuthService(OauthProvider provider) {
    if (!serviceMap.containsKey(provider)) {
      throw BusinessException.error(ErrorCode.OAUTH_UNSUPPORTED_PROVIDER);
    }
    return serviceMap.get(provider);
  }
}
