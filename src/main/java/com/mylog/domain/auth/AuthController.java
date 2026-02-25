package com.mylog.domain.auth;

import com.mylog.common.annotations.MemberId;
import com.mylog.common.response.SuccessResponse;
import com.mylog.domain.auth.dto.request.LoginRequest;
import com.mylog.domain.auth.dto.request.OAuthRequest;
import com.mylog.domain.auth.dto.request.RefreshRequest;
import com.mylog.domain.auth.dto.request.SignUpRequest;
import com.mylog.domain.auth.dto.response.LoginResponse;
import com.mylog.domain.auth.dto.response.RefreshResponse;
import com.mylog.domain.auth.service.AuthService;
import com.mylog.domain.auth.service.oauth.OAuthUserServiceFactory;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final OAuthUserServiceFactory oAuth2UserServiceFactory;


    @PostMapping("/register")
    @Operation(summary = "회원가입")
    public ResponseEntity<SuccessResponse<Void>> signUp(@RequestBody @Valid SignUpRequest request){
        authService.signUp(request);
        return SuccessResponse.toOk(null);
    }

    @Operation(summary = "이메일 로그인")
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<LoginResponse>> login(@RequestBody LoginRequest request){
        LoginResponse response = authService.login(request);
        return SuccessResponse.toOk(response);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Void>> logout(
        @RequestHeader("Authorization") String authHeader,
        @MemberId Long memberId
    ){
        authService.logout(authHeader, memberId);
        return SuccessResponse.toOk(null);
    }

    @Operation(summary = "토큰 리프레시")
    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponse<RefreshResponse>> refresh(@RequestBody RefreshRequest request){
        RefreshResponse response = authService.refresh(request);
        return SuccessResponse.toOk(response);
    }

    @Operation(summary = "소셜 로그인")
    @PostMapping("/oauth/login")
    public ResponseEntity<SuccessResponse<LoginResponse>> socialLogin(@RequestBody @Valid OAuthRequest request){
        LoginResponse response = oAuth2UserServiceFactory
            .getOAuth2UserService(request.provider())
            .authenticate(request);
        return SuccessResponse.toOk(response);
    }

}
