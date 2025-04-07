package com.mylog.controller;

import com.mylog.common.CommonResult;
import com.mylog.common.ResponseService;
import com.mylog.service.notification.CommonNotificationService;
import com.mylog.service.notification.NotificationServiceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationServiceFactory factory;
    private final CommonNotificationService notificationService;

    //알림 조회

    //알림 읽기
    @PutMapping("/{id}")
    public CommonResult readNotification(@PathVariable Long id){
        notificationService.readNotification(id);
        return ResponseService.getSuccessResult();
    }


    //알림 끄기/켜기

}
