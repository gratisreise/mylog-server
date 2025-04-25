package com.mylog.controller;

import com.mylog.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RequestMapping("/test")
@RequiredArgsConstructor
@RestController
public class TestController {

    private final S3Service s3Service;


    @PostMapping("/delete")
    public void delete(){
        String url = "https://mylog-imgsource.s3.ap-northeast-2.amazonaws.com/b5d98eb4-5e5f-401c-bf3c-251ef55feb9c_basic.png";
        s3Service.deleteImage(url);
    }
}
