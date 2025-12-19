package com.mylog.controller;

import com.mylog.common.CommonResult;
import com.mylog.common.ListResult;
import com.mylog.common.ResponseService;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.api.notification.NotificationSettingResponse;
import com.mylog.service.notificationsetting.NotificationSettingReader;
import com.mylog.service.notificationsetting.NotificationSettingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications/settings")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;
    private final NotificationSettingReader notificationSettingReader;

    @GetMapping
    @Operation(summary = "알림설정조회")
    public ListResult<NotificationSettingResponse> getNotificationsSettings(
        @AuthenticationPrincipal CustomUser customUser
    ){
        return ResponseService.getListResult(notificationSettingReader.getNotificationSettings(customUser));
    }

    //알림 끄기/켜기
    @PutMapping("/{type}")
    @Operation(summary = "알림 토글")
    public CommonResult toggleNotification(
        @AuthenticationPrincipal CustomUser customUser,
        @PathVariable String type
    ){
        notificationSettingService.toggleNotification(customUser, type);
        return ResponseService.getSuccessResult();
    }

}
