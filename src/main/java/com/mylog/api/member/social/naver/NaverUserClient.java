package com.mylog.api.member.social.naver;

import com.mylog.api.auth.social.naver.NaverUserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "naverUserClient", url = "https://openapi.naver.com")
public interface NaverUserClient {
    @GetMapping("/v1/nid/me")
    NaverUserInfo getUserInfo(@RequestHeader("Authorization") String bearerToken);
}
