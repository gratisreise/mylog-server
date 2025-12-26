package com.mylog.api.notification.dto;


import com.mylog.api.notificationsetting.entity.NotificationSetting;

public record NotificationSettingResponse(String type, boolean disabled) {
    public NotificationSettingResponse(NotificationSetting setting){
       this(setting.getType(), setting.isDisabled());
    }
}
