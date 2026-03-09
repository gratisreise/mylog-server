package com.mylog.domain.notification;

import com.mylog.common.annotations.MemberId;
import com.mylog.common.response.PageResponse;
import com.mylog.common.response.SuccessResponse;
import com.mylog.domain.notification.dto.NotificationResponse;
import com.mylog.domain.notification.service.NotificationReader;
import com.mylog.domain.notification.service.NotificationWriter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController {
  private final NotificationWriter notificationWriter;
  private final NotificationReader notificationReader;

  // 알림 조회
  @GetMapping
  @Operation(summary = "알림 목록 조회", description = "사용자의 알림을 페이징하여 조회")
  public ResponseEntity<SuccessResponse<PageResponse<NotificationResponse>>> getNotifications(
      @MemberId Long memberId, @PageableDefault Pageable pageable) {
    Page<NotificationResponse> page = notificationReader.receiveNotification(memberId, pageable);
    return SuccessResponse.toOk(PageResponse.from(page));
  }

  // 알림 읽음 처리
  @PutMapping("/{id}")
  @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경")
  public ResponseEntity<SuccessResponse<Void>> readNotification(
      @MemberId Long memberId, @PathVariable @Positive Long id) {
    notificationWriter.readNotification(memberId, id);
    return SuccessResponse.toNoContent();
  }
}
