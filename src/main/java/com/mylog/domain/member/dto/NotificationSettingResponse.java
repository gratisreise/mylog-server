package com.mylog.domain.member.dto;

import com.mylog.domain.member.entity.NotificationSetting;
import lombok.Builder;

@Builder
public record NotificationSettingResponse(String type, boolean disabled) {

  public static NotificationSettingResponse from(NotificationSetting setting) {
    return  NotificationSettingResponse.builder()
        .type(setting.getType())
        .disabled(setting.isDisabled())
        .build();
  }
}
