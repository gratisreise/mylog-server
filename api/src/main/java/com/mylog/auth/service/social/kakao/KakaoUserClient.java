package com.mylog.auth.service.social.kakao;

import com.mylog.auth.dto.social.kako.KakaoUserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "kakaoUser", url="https://kapi.kakao.com")
public interface KakaoUserClient {
    @GetMapping("/v2/user/me")
    KakaoUserInfo getUserInfo(@RequestHeader("Authorization") String bearerToken);
}
