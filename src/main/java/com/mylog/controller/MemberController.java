package com.mylog.controller;

import com.mylog.common.CommonResult;
import com.mylog.common.ResponseService;
import com.mylog.common.SingleResult;
import com.mylog.dto.SignUpRequest;
import com.mylog.dto.UpdateMemberRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Member;
import com.mylog.service.member.MemberService;
import com.mylog.service.member.MemberServiceFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberServiceFactory factory;

    @PostMapping("/sign-up")
    public CommonResult signUp(@RequestBody @Valid SignUpRequest request){
        MemberService service = factory.getMemberService(request.getProvider());
        service.saveMember(request);
        return ResponseService.getSuccessResult();
    }

    @GetMapping("/me")
    public SingleResult<Member> getMember(@AuthenticationPrincipal CustomUser customUser){
        MemberService service = factory.getMemberService(customUser.getProvider());
        return ResponseService.getSingleResult(service.getMember(customUser));
    }







}
