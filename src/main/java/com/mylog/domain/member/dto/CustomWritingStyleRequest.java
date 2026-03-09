package com.mylog.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomWritingStyleRequest(
    @NotBlank(message = "스타일 이름은 필수입니다.") @Size(max = 30, message = "스타일 이름은 30자 이하여야 합니다.") String name,
    @NotBlank(message = "역할은 필수입니다.") @Size(max = 50, message = "역할은 50자 이하여야 합니다.") String role,
    @NotBlank(message = "지시사항은 필수입니다.") @Size(max = 200, message = "지시사항은 200자 이하여야 합니다.") String instruction) {}
