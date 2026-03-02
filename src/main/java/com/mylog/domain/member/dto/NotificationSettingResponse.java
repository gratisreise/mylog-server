package com.mylog.domain.member.dto;

import com.mylog.domain.member.entity.NotificationSetting;

public record NotificationSettingResponse(String type, boolean disabled) {
    public NotificationSettingResponse(NotificationSetting setting){
       this(setting.getType(), setting.isDisabled());
    }
}
