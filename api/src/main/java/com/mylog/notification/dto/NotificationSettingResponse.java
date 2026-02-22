package com.mylog.notification.dto;


import com.mylog.notification.entity.NotificationSetting;
import lombok.Builder;

@Builder
public record NotificationSettingResponse(String type, boolean disabled) {

    public static NotificationSettingResponse from(NotificationSetting setting){
        return NotificationSettingResponse.builder()
            .type(setting.getType())
            .disabled(setting.isDisabled())
            .build();
    }
}
