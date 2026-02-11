package com.mylog.domain.notificationsetting;

import com.mylog.domain.notificationsetting.service.NotificationSettingReader;
import com.mylog.domain.notificationsetting.service.NotificationSettingWriter;
import com.mylog.common.response.CommonResult;
import com.mylog.common.response.ListResult;
import com.mylog.common.response.ResponseService;
import com.mylog.common.security.CustomUser;
import com.mylog.domain.notification.dto.NotificationSettingResponse;
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

    private final NotificationSettingWriter notificationSettingWriter;
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
        notificationSettingWriter.toggleNotification(customUser, type);
        return ResponseService.getSuccessResult();
    }

}
