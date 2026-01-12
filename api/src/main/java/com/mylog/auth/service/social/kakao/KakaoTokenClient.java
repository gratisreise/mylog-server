package com.mylog.auth.service.social.kakao;

import com.mylog.auth.dto.social.kako.KakaoTokenResponse;
import com.mylog.config.FeignConfig;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "kakaoToken",
    url="https://kauth.kakao.com",
    configuration = FeignConfig.class
)
public interface KakaoTokenClient {
    @PostMapping( value = "/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    KakaoTokenResponse getAccessToken(@RequestBody Map<String, ?> formParams);
}
