package com.mylog.domain.notification;

import com.mylog.common.annotations.MemberId;
import com.mylog.common.response.PageResponse;
import com.mylog.common.response.SuccessResponse;
import com.mylog.common.security.CustomUser;
import com.mylog.domain.notification.dto.NotificationResponse;
import com.mylog.domain.notification.service.NotificationReader;
import com.mylog.domain.notification.service.NotificationWriter;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationWriter notificationWriter;
    private final NotificationReader notificationReader;

    //알림 조회
    @GetMapping
    @Operation(summary = "알림조회")
    public ResponseEntity<SuccessResponse<PageResponse<NotificationResponse>>> getNotifications(
        @MemberId Long memberId,
        @PageableDefault Pageable pageable
    ) {
        Page<NotificationResponse> page = notificationReader.receiveNotification(memberId, pageable);
        return SuccessResponse.toOk(PageResponse.from(page));
    }

    //알림 읽기
    @PutMapping("/{id}")
    @Operation(summary = "알림 읽기")
    public ResponseEntity<Void> readNotification(@PathVariable Long id) {
        notificationWriter.readNotification(id);
        return ResponseEntity.noContent().build();
    }

}
