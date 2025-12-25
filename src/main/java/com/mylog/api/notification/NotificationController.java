package com.mylog.api.notification;

import com.mylog.common.CommonResult;
import com.mylog.common.ResponseService;
import com.mylog.common.SingleResult;
import com.mylog.api.auth.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final NotificationWriter notificationWriter;
    private final NotificationReader notificationReader;
    //알림 조회
    @GetMapping
    @Operation(summary = "알림조회")
    public SingleResult<Page<NotificationResponse>> getNotifications(
        @AuthenticationPrincipal CustomUser customUser,
        @PageableDefault Pageable pageable
    ){
        return ResponseService.getSingleResult(notificationReader.receiveNotification(customUser, pageable));
    }

    //알림 읽기
    @PutMapping("/{id}")
    @Operation(summary = "알림 읽기")
    public CommonResult readNotification(@PathVariable Long id){
        notificationWriter.readNotification(id);
        return ResponseService.getSuccessResult();
    }

}
