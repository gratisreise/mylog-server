package com.mylog.api.auth.controller;

import com.mylog.api.auth.service.AuthService;
import com.mylog.api.auth.dto.LoginRequest;
import com.mylog.api.auth.dto.LoginResponse;
import com.mylog.api.auth.dto.RefreshRequest;
import com.mylog.api.auth.dto.RefreshResponse;
import com.mylog.common.ResponseService;
import com.mylog.common.SingleResult;
import com.mylog.api.auth.dto.social.OAuthRequest;
import com.mylog.api.auth.service.social.OAuth2UserService;
import com.mylog.api.auth.service.social.OAuth2UserServiceFactory;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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
    
    @Operation(summary = "이메일 로그인")
    @PostMapping("/login")
    public SingleResult<LoginResponse> login(@RequestBody LoginRequest request){
        return ResponseService.getSingleResult(authService.login(request));
    }

    @Operation(summary = "토큰 리프레시")
    @PostMapping("/refresh")
    public SingleResult<RefreshResponse> refresh(@RequestBody RefreshRequest request){
        return ResponseService.getSingleResult(authService.refresh(request));
    }

    @Operation(summary = "소셜 로그인")
    @PostMapping("/oauth/login")
    public SingleResult<LoginResponse> socialLogin(@RequestBody @Valid OAuthRequest request){
        OAuth2UserService service = oAuth2UserServiceFactory.getOAuth2UserService(request.getProvider());
        return ResponseService.getSingleResult(service.login(request));
    }

}