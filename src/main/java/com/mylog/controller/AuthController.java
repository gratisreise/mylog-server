package com.mylog.controller;

import com.mylog.common.ResponseService;
import com.mylog.common.SingleResult;
import com.mylog.config.JwtTokenProvider;
import com.mylog.dto.LoginRequest;
import com.mylog.dto.LoginResponse;
import com.mylog.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public SingleResult<LoginResponse> login(@RequestBody LoginRequest request){
        return ResponseService.getSingleResult(new LoginResponse(authService.login(request)));
    }

}
