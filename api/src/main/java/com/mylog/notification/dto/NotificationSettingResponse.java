package com.mylog.notification.dto;


import com.mylog.notification.entity.NotificationSetting;

public record NotificationSettingResponse(String type, boolean disabled) {
    public NotificationSettingResponse(NotificationSetting setting){
       this(setting.getType(), setting.isDisabled());
    }
}
