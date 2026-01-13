package com.mylog.member;


import com.mylog.auth.classes.CustomUser;
import com.mylog.member.dto.MemberResponse;
import com.mylog.member.dto.MemberUpdateRequest;
import com.mylog.response.CommonResult;
import com.mylog.response.ResponseService;
import com.mylog.response.SingleResult;
import com.mylog.s3.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {
   private final MemberService memberService;
   private final S3Service s3Service;

    //개인정보조회
    @GetMapping("/me")
    @Operation(summary = "개인정보조회")
    public SingleResult<MemberResponse> getMember(@AuthenticationPrincipal CustomUser customUser){
        return ResponseService.getSingleResult(memberService.getMember(customUser));
    }

    //개인정보 수정
    @PutMapping("/me")
    @Operation(summary = "개인정보수정")
    public CommonResult updateMember(
        @RequestPart(value="request") @Valid MemberUpdateRequest request,
        @RequestPart(required = false, value="file") MultipartFile file,
        @AuthenticationPrincipal CustomUser customUser
    ) {
        String imageUrl = s3Service.upload(file);
        memberService.updateMember(request, imageUrl, customUser);
        return ResponseService.getSuccessResult();
    }

    //개인정보 삭제
    @DeleteMapping("/me")
    @Operation(summary = "개인정보삭제")
    public CommonResult deleteMember(@AuthenticationPrincipal CustomUser customUser){
        memberService.deleteMember(customUser);
        return ResponseService.getSuccessResult();
    }

}