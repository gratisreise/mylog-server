package com.mylog.controller;

import com.mylog.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RequestMapping("/tests")
@RequiredArgsConstructor
@RestController
public class TestController {

    private final S3Service s3Service;

    @GetMapping
    public String test(){
        return "Success";
    }

    @PostMapping("/delete")
    public void delete(){
    }
}
