package com.mylog.dto.social;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KakaoUserInfo {
    private Long id; // 카카오 회원번호 (필수)

    @JsonProperty("connected_at")
    private String connectedAt; // 연결 시각 (필수)

    private KakaoProperties properties; // 프로필 이미지 등

}
