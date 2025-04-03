package com.mylog.controller;

import com.mylog.service.category.CategoryServiceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryServiceFactory factory;

    //카테고리 생성

    //카테고리 조회

    //카테고리 수저

    //카테고리 삭제

}
