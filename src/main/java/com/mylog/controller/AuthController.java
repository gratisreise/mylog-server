package com.mylog.controller;

import com.mylog.common.ResponseService;
import com.mylog.common.SingleResult;
import com.mylog.dto.LoginRequest;
import com.mylog.dto.LoginResponse;

import com.mylog.dto.social.OAuthRequest;
import com.mylog.dto.RefreshRequest;
import com.mylog.dto.RefreshResponse;
import com.mylog.enums.OauthProvider;
import com.mylog.interfaces.OAuth2UserService;
import com.mylog.service.AuthService;

import com.mylog.service.social.OAuth2UserServiceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final OAuth2UserServiceFactory oAuth2UserServiceFactory;

    @PostMapping("/login")
    public SingleResult<LoginResponse> login(@RequestBody LoginRequest request){
        return ResponseService.getSingleResult(authService.login(request));
    }

    @PostMapping("/refresh")
    public SingleResult<RefreshResponse> refresh(@RequestBody RefreshRequest request){
        return ResponseService.getSingleResult(authService.refresh(request));
    }


    @PostMapping("/oauth/login")
    public SingleResult<LoginResponse> socialLogin(@RequestBody OAuthRequest request){
        OAuth2UserService service = oAuth2UserServiceFactory.getOAuth2UserService(request.getProvider());
        return ResponseService.getSingleResult(service.login(request));
    }

//    @PostMapping("/oauth/naver/login")
//    public SingleResult<String> tokenTest(@RequestBody OAuthRequest request){
//        log.info("Received authorization code: {}", request.getCode());
//        log.info("Provider: {}", request.getProvider());
//        return ResponseService.getSingleResult(oauthService.getNaverToken(request));
//    }

}
