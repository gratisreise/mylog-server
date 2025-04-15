package com.mylog.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class TestRequest {
    private MultipartFile image;

}
