package com.mylog.controller;

import com.mylog.common.CommonResult;
import com.mylog.common.ResponseService;
import com.mylog.common.SingleResult;
import com.mylog.dto.SignUpRequest;
import com.mylog.dto.UpdateMemberRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Member;
import com.mylog.service.member.CommonMemberService;
import com.mylog.service.member.MemberService;
import com.mylog.service.member.MemberServiceFactory;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberServiceFactory factory;
    private final CommonMemberService memberService;

    @PostMapping("/sign-up")
    public CommonResult signUp(@RequestBody @Valid SignUpRequest request){
        memberService.saveMember(request);
        return ResponseService.getSuccessResult();
    }

    @GetMapping("/me")
    public SingleResult<Member> getMember(@AuthenticationPrincipal CustomUser customUser){
        MemberService service = factory.getMemberService(customUser.getProvider());
        return ResponseService.getSingleResult(service.getMember(customUser));
    }


    @PutMapping("/me")
    public CommonResult updateMember(
        @RequestPart(value="request") @Valid UpdateMemberRequest request,
        @RequestPart(value="file") MultipartFile file,
        @AuthenticationPrincipal CustomUser customUser
    ) throws IOException {
        MemberService service = factory.getMemberService(customUser.getProvider());
        service.updateMember(request, customUser, file);
        return ResponseService.getSuccessResult();
    }

    @DeleteMapping("/me")
    public CommonResult deleteMember(@AuthenticationPrincipal CustomUser customUser){
        MemberService service = factory.getMemberService(customUser.getProvider());
        service.deleteMember(customUser);
        return ResponseService.getSuccessResult();
    }



}
