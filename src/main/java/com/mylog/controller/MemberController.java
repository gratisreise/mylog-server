package com.mylog.controller;

import com.mylog.common.CommonResult;
import com.mylog.common.ResponseService;
import com.mylog.common.SingleResult;
import com.mylog.dto.SignUpRequest;
import com.mylog.entity.Member;
import com.mylog.service.CustomUserDetailsService;
import com.mylog.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/sign-up")
    public CommonResult signUp(@RequestBody @Valid SignUpRequest request){
        memberService.saveMember(request);
        return ResponseService.getSuccessResult();
    }

    @GetMapping("/me")
    public SingleResult<Member> getMember(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseService.getSingleResult(memberService.getMember());
    }


}
