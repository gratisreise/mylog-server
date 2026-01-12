package com.mylog.auth.service.social.google;

import com.mylog.auth.dto.social.google.GoogleTokenResponse;
import com.mylog.config.FeignConfig;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "googleToken",
    url = "https://oauth2.googleapis.com",
    configuration = FeignConfig.class
)
public interface GoogleTokenClient {
    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    GoogleTokenResponse getAccessToken(@RequestBody Map<String, ?> formParams);
}

