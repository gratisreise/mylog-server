package com.mylog.domain.notification.dto;


import com.mylog.domain.notificationsetting.NotificationSetting;

public record NotificationSettingResponse(String type, boolean disabled) {
    public NotificationSettingResponse(NotificationSetting setting){
       this(setting.getType(), setting.isDisabled());
    }
}
