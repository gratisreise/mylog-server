package com.mylog.controller;

import com.mylog.common.CommonResult;
import com.mylog.common.ResponseService;
import com.mylog.common.SingleResult;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.dto.notification.NotificationResponse;
import com.mylog.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
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
    private final NotificationService notificationService;

    //알림 조회
    @GetMapping
    @Operation(summary = "알림조회")
    public SingleResult<Page<NotificationResponse>> getNotifications(
        @AuthenticationPrincipal CustomUser customUser,
        @PageableDefault(sort="createdAt", direction = Direction.DESC)
        Pageable pageable
    ){
        return ResponseService.getSingleResult(notificationService.receiveNotification(customUser, pageable));
    }

    //알림 읽기
    @PutMapping("/{id}")
    @Operation(summary = "알림 읽기")
    public CommonResult readNotification(@PathVariable Long id){
        notificationService.readNotification(id);
        return ResponseService.getSuccessResult();
    }


    //알림 끄기/켜기
    @PutMapping("/set/{type}")
    @Operation(summary = "알림 토글")
    public CommonResult toggleNotification(
        @AuthenticationPrincipal CustomUser customUser,
        @PathVariable String type
    ){
        notificationService.toggleNotification(customUser, type);
        return ResponseService.getSuccessResult();
    }

}
