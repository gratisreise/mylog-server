package com.mylog.service.social.google;

import com.mylog.config.FeignConfig;
import com.mylog.model.dto.social.google.GoogleUserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "googleUser",
    url = "https://openidconnect.googleapis.com",
    configuration = FeignConfig.class
)
public interface GoogleUserClient {
    @GetMapping("/v1/userinfo")
    GoogleUserInfo getUserInfo(@RequestHeader("Authorization") String bearerToken);
}
