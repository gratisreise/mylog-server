package com.mylog.auth;

import com.mylog.auth.classes.CustomUser;
import com.mylog.auth.dto.LoginRequest;
import com.mylog.auth.dto.LoginResponse;
import com.mylog.auth.dto.RefreshRequest;
import com.mylog.auth.dto.RefreshResponse;
import com.mylog.auth.dto.SignUpRequest;
import com.mylog.auth.dto.social.OAuthRequest;
import com.mylog.auth.service.AuthService;
import com.mylog.auth.service.social.OAuth2UserService;
import com.mylog.auth.service.social.OAuth2UserServiceFactory;
import com.mylog.member.MemberService;
import com.mylog.response.CommonResult;
import com.mylog.response.ResponseService;
import com.mylog.response.SingleResult;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final OAuth2UserServiceFactory oAuth2UserServiceFactory;
    private final MemberService memberService;

    @PostMapping("/register")
    @Operation(summary = "회원가입")
    public CommonResult signUp(@RequestBody @Valid SignUpRequest request){
        memberService.saveMember(request);
        return ResponseService.getSuccessResult();
    }
    
    @Operation(summary = "이메일 로그인")
    @PostMapping("/login")
    public SingleResult<LoginResponse> login(@RequestBody LoginRequest request){
        return ResponseService.getSingleResult(authService.login(request));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public CommonResult logout(
        @RequestHeader("Authorization") String authHeader,
        @AuthenticationPrincipal CustomUser customUser
    ){
        authService.logout(authHeader, customUser.getMemberId());
        return ResponseService.getSuccessResult();
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