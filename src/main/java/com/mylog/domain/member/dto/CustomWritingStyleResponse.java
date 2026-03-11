package com.mylog.domain.member.dto;

import com.mylog.domain.member.entity.CustomWritingStyle;

public record CustomWritingStyleResponse(Long id, String name, String role, String instruction) {
  public static CustomWritingStyleResponse from(CustomWritingStyle style) {
    return new CustomWritingStyleResponse(
        style.getId(), style.getName(), style.getRole(), style.getInstruction());
  }
}
