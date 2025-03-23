package com.mylog.controller;

import com.mylog.common.ResponseService;
import com.mylog.common.SingleResult;
import com.mylog.dto.GoogleTokenResponse;
import com.mylog.dto.LoginRequest;
import com.mylog.dto.LoginResponse;

import com.mylog.dto.OAuthRequest;
import com.mylog.dto.RefreshRequest;
import com.mylog.dto.RefreshResponse;
import com.mylog.service.AuthService;

import com.mylog.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final OAuthService oauthService;

    @PostMapping("/login")
    public SingleResult<LoginResponse> login(@RequestBody LoginRequest request){
        return ResponseService.getSingleResult(authService.login(request));
    }

    @PostMapping("/refresh")
    public SingleResult<RefreshResponse> refresh(@RequestBody RefreshRequest request){
        return ResponseService.getSingleResult(authService.refresh(request));
    }


    @PostMapping("/oauth/login")
    public SingleResult<LoginResponse> tokenTest(@RequestBody OAuthRequest request){
        return ResponseService.getSingleResult(oauthService.socialLogin(request));
    }



}
