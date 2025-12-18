package com.mylog.api.member;

import com.mylog.common.CommonResult;
import com.mylog.common.ResponseService;
import com.mylog.common.SingleResult;
import com.mylog.model.dto.classes.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@Slf4j
public class MemberController {
    private final MemberReader memberReader;
    private final MemberWriter memberWriter;

    //회원가입
    @PostMapping("/sign-up")
    @Operation(summary = "회원가입")
    public CommonResult signUp(@RequestBody @Valid SignUpRequest request){
        memberWriter.saveMember(request);
        return ResponseService.getSuccessResult();
    }

    //개인정보조회
    @GetMapping("/me")
    @Operation(summary = "개인정보조회")
    public SingleResult<MemberResponse> getMember(@AuthenticationPrincipal CustomUser customUser){
        return ResponseService.getSingleResult(memberReader.getMember(customUser));
    }

    //개인정보 수정
    @PutMapping("/me")
    @Operation(summary = "개인정보수정")
    public CommonResult updateMember(
        @RequestPart(value="request") @Valid UpdateMemberRequest request,
        @RequestPart(required = false, value="file") MultipartFile file,
        @AuthenticationPrincipal CustomUser customUser
    ) throws IOException {
        memberWriter.updateMember(request, customUser, file);
        return ResponseService.getSuccessResult();
    }


    //개인정보 삭제
    @DeleteMapping("/me")
    @Operation(summary = "개인정보삭제")
    public CommonResult deleteMember(@AuthenticationPrincipal CustomUser customUser){
        memberWriter.deleteMember(customUser);
        return ResponseService.getSuccessResult();
    }

}