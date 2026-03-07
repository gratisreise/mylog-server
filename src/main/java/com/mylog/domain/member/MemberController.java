package com.mylog.domain.member;


import com.mylog.common.annotations.MemberId;
import com.mylog.common.response.SuccessResponse;
import com.mylog.domain.member.dto.MemberResponse;
import com.mylog.external.s3.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<SuccessResponse<MemberResponse>> getMember(
        @MemberId Long memberId
    ) {
        return SuccessResponse.toOk(memberService.getMember(customUser));
    }

    //개인정보 수정
    @PutMapping("/me")
    @Operation(summary = "개인정보수정")
    public ResponseEntity<SuccessResponse<Void>> updateMember(
        @RequestPart(value = "request") @Valid MemberUpdateRequest request,
        @RequestPart(required = false, value = "file") MultipartFile file,
        @MemberId Long memberId
    ) {
        String imageUrl = s3Service.upload(file);
        memberService.updateMember(request, imageUrl, customUser);
        return SuccessResponse.toOk(null);
    }

    //개인정보 삭제
    @DeleteMapping("/me")
    @Operation(summary = "개인정보삭제")
    public ResponseEntity<SuccessResponse<Void>> deleteMember(
        @MemberId Long memberId
    ) {
        memberService.deleteMember(customUser);
        return SuccessResponse.toOk(null);
    }
}
