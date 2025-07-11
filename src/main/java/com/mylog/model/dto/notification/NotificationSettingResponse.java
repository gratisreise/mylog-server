package com.mylog.model.dto.notification;


import com.mylog.model.entity.NotificationSetting;

public record NotificationSettingResponse(String type, boolean disabled) {
    public NotificationSettingResponse(NotificationSetting setting){
       this(setting.getType(), setting.isDisabled());
    }
}
