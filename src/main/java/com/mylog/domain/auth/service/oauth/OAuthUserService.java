package com.mylog.domain.auth.service.oauth;

import com.mylog.domain.auth.dto.request.OAuthRequest;
import com.mylog.domain.auth.dto.response.LoginResponse;

public interface OAuthUserService {
  LoginResponse authenticate(OAuthRequest request);
}
