package com.mylog.domain.member;

import com.mylog.common.annotations.AuthenticatedMember;
import com.mylog.common.response.SuccessResponse;
import com.mylog.domain.member.dto.CustomWritingStyleRequest;
import com.mylog.domain.member.dto.CustomWritingStyleResponse;
import com.mylog.domain.member.dto.MemberResponse;
import com.mylog.domain.member.dto.NotificationSettingResponse;
import com.mylog.domain.member.dto.UpdateMemberRequest;
import com.mylog.domain.member.entity.CustomWritingStyle;
import com.mylog.domain.member.service.CustomWritingStyleReader;
import com.mylog.domain.member.service.CustomWritingStyleWriter;
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
  private final MemberService memberService;
  private final S3Service s3Service;
  private final NotificationSettingReader notificationSettingReader;
  private final NotificationSettingWriter notificationSettingWriter;
  private final CustomWritingStyleReader customWritingStyleReader;
  private final CustomWritingStyleWriter customWritingStyleWriter;

  @Operation(summary = "개인정보조회")
  @GetMapping("/me")
  public ResponseEntity<SuccessResponse<MemberResponse>> getMember(
      @AuthenticatedMember Long memberId) {
    return SuccessResponse.toOk(memberService.getMember(memberId));
  }

  @Operation(summary = "개인정보수정")
  @PatchMapping("/me")
  public ResponseEntity<SuccessResponse<Void>> updateMember(
      @RequestPart(value = "request") @Valid UpdateMemberRequest request,
      @RequestPart(required = false, value = "file") MultipartFile file,
      @AuthenticatedMember Long memberId) {
    String imageUrl = (file != null && !file.isEmpty()) ? s3Service.upload(file) : null;
    memberService.updateMember(request, imageUrl, memberId);
    return SuccessResponse.toNoContent();
  }

  @Operation(summary = "회원탈퇴")
  @DeleteMapping("/me")
  public ResponseEntity<SuccessResponse<Void>> deleteMember(@AuthenticatedMember Long memberId) {
    memberService.delete(memberId);
    return SuccessResponse.toNoContent();
  }

  @Operation(summary = "알림 설정 조회")
  @GetMapping("/me/notification-settings")
  public ResponseEntity<SuccessResponse<List<NotificationSettingResponse>>> getNotificationSettings(
      @AuthenticatedMember Long memberId) {
    return SuccessResponse.toOk(notificationSettingReader.getNotificationSettings(memberId));
  }

  @Operation(summary = "알림 토글")
  @PutMapping("/me/notification-settings/{type}")
  public ResponseEntity<SuccessResponse<Void>> toggleNotification(
      @AuthenticatedMember Long memberId, @PathVariable String type) {
    notificationSettingWriter.toggleNotification(memberId, type);
    return SuccessResponse.toNoContent();
  }

  // === 커스텀 문체 스타일 ===

  @Operation(summary = "커스텀 문체 스타일 생성")
  @PostMapping("/me/custom-styles")
  public ResponseEntity<SuccessResponse<CustomWritingStyleResponse>> createCustomStyle(
      @AuthenticatedMember Long memberId, @RequestBody @Valid CustomWritingStyleRequest request) {
    CustomWritingStyle style =
        customWritingStyleWriter.create(
            memberId, request.name(), request.role(), request.instruction());
    return SuccessResponse.toOk(CustomWritingStyleResponse.from(style));
  }

  @Operation(summary = "커스텀 문체 스타일 목록 조회")
  @GetMapping("/me/custom-styles")
  public ResponseEntity<SuccessResponse<List<CustomWritingStyleResponse>>> getCustomStyles(
      @AuthenticatedMember Long memberId) {
    List<CustomWritingStyleResponse> styles =
        customWritingStyleReader.getCustomStyles(memberId).stream()
            .map(CustomWritingStyleResponse::from)
            .toList();
    return SuccessResponse.toOk(styles);
  }

  @Operation(summary = "커스텀 문체 스타일 수정")
  @PutMapping("/me/custom-styles/{styleId}")
  public ResponseEntity<SuccessResponse<Void>> updateCustomStyle(
      @AuthenticatedMember Long memberId,
      @PathVariable Long styleId,
      @RequestBody @Valid CustomWritingStyleRequest request) {
    customWritingStyleWriter.update(
        styleId, memberId, request.name(), request.role(), request.instruction());
    return SuccessResponse.toNoContent();
  }

  @Operation(summary = "커스텀 문체 스타일 삭제")
  @DeleteMapping("/me/custom-styles/{styleId}")
  public ResponseEntity<SuccessResponse<Void>> deleteCustomStyle(
      @AuthenticatedMember Long memberId, @PathVariable Long styleId) {
    customWritingStyleWriter.delete(styleId, memberId);
    return SuccessResponse.toNoContent();
  }
}
