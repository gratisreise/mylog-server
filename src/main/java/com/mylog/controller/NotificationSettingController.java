package com.mylog.controller;

import com.mylog.common.ListResult;
import com.mylog.common.ResponseService;
import com.mylog.common.SingleResult;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.dto.notification.NotificationSettingResponse;
import com.mylog.model.entity.NotificationSetting;
import com.mylog.service.NotificationSettingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications/settings")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    @GetMapping
    @Operation(summary = "알림설정조회")
    public ListResult<NotificationSettingResponse> getNotificationsSettings(
        @AuthenticationPrincipal CustomUser customUser
    ){
        return ResponseService.getListResult(notificationSettingService.
            getNotificationSettings(customUser));
    }

}
