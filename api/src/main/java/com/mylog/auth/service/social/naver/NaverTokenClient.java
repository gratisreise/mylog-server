package com.mylog.auth.service.social.naver;

import com.mylog.auth.dto.social.naver.NaverTokenResponse;
import com.mylog.config.FeignConfig;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "naverTokenClient",
    url = "https://nid.naver.com",
    configuration = FeignConfig.class
)
public interface NaverTokenClient {
    @PostMapping(value = "/oauth2.0/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    NaverTokenResponse getAccessToken(@RequestBody Map<String, ?> params);
}
