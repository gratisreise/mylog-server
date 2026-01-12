package com.mylog.notification.controller;

import com.mylog.auth.classes.CustomUser;
import com.mylog.common.PageResponse;
import com.mylog.notification.NotificationService;
import com.mylog.notification.dto.NotificationResponse;
import com.mylog.notification.dto.NotificationSettingResponse;
import com.mylog.response.CommonResult;
import com.mylog.response.ListResult;
import com.mylog.response.ResponseService;
import com.mylog.response.SingleResult;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    //알림 조회
    @GetMapping
    @Operation(summary = "알림조회")
    public SingleResult<PageResponse<NotificationResponse>> getNotifications(
        @AuthenticationPrincipal CustomUser customUser,
        @PageableDefault Pageable pageable
    ){
        return ResponseService.getSingleResult(
            notificationService.receiveNotification(customUser.getMemberId(), pageable));
    }

    //알림 읽기
    @PatchMapping("/{notificatoinId}")
    @Operation(summary = "알림 읽기")
    public CommonResult readNotification(@PathVariable Long notificatoinId){
        notificationService.readNotification(notificatoinId);
        return ResponseService.getSuccessResult();
    }

    @GetMapping("/settings")
    @Operation(summary = "알림설정조회")
    public ListResult<NotificationSettingResponse> getNotificationsSettings(
        @AuthenticationPrincipal CustomUser customUser
    ){
        return ResponseService.getListResult(
            notificationService.getNotificationSettings(customUser.getMemberId()));
    }

    //알림 끄기/켜기
    @PatchMapping("/{type}")
    @Operation(summary = "알림 토글")
    public CommonResult toggleNotification(
        @AuthenticationPrincipal CustomUser customUser,
        @PathVariable String type
    ){
        notificationService.toggleNotification(customUser.getMemberId(), type);
        return ResponseService.getSuccessResult();
    }

}
