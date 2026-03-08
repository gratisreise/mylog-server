package com.mylog.domain.member;

import com.mylog.common.annotations.MemberId;
import com.mylog.common.response.SuccessResponse;
import com.mylog.domain.member.dto.MemberResponse;
import com.mylog.domain.member.dto.NotificationSettingResponse;
import com.mylog.domain.member.dto.UpdateMemberRequest;
import com.mylog.domain.member.service.NotificationSettingReader;
import com.mylog.domain.member.service.NotificationSettingWriter;
import com.mylog.external.s3.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final NotificationSettingReader notificationSettingReader;
    private final NotificationSettingWriter notificationSettingWriter;

    @GetMapping("/me")
    @Operation(summary = "개인정보조회")
    public ResponseEntity<SuccessResponse<MemberResponse>> getMember(
        @MemberId Long memberId
    ) {
        return SuccessResponse.toOk(memberService.getMember(memberId));
    }

    @PatchMapping("/me")
    @Operation(summary = "개인정보수정")
    public ResponseEntity<SuccessResponse<Void>> updateMember(
        @RequestPart(value = "request") @Valid UpdateMemberRequest request,
        @RequestPart(required = false, value = "file") MultipartFile file,
        @MemberId Long memberId
    ) {
        String imageUrl = (file != null && !file.isEmpty()) ? s3Service.upload(file) : null;
        memberService.updateMember(request, imageUrl, memberId);
        return SuccessResponse.toNoContent();
    }

    @DeleteMapping("/me")
    @Operation(summary = "개인정보삭제")
    public ResponseEntity<SuccessResponse<Void>> deleteMember(
        @MemberId Long memberId
    ) {
        memberService.delete(memberId);
        return SuccessResponse.toNoContent();
    }

    @GetMapping("/me/notification-settings")
    @Operation(summary = "알림 설정 조회")
    public ResponseEntity<SuccessResponse<List<NotificationSettingResponse>>> getNotificationSettings(
        @MemberId Long memberId
    ) {
        return SuccessResponse.toOk(notificationSettingReader.getNotificationSettings(memberId));
    }

    @PutMapping("/me/notification-settings/{type}")
    @Operation(summary = "알림 토글")
    public ResponseEntity<SuccessResponse<Void>> toggleNotification(
        @MemberId Long memberId,
        @PathVariable String type
    ) {
        notificationSettingWriter.toggleNotification(memberId, type);
        return SuccessResponse.toNoContent();
    }
}
