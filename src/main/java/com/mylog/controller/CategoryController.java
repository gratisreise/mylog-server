package com.mylog.controller;

import com.mylog.common.CommonResult;
import com.mylog.common.ListResult;
import com.mylog.common.ResponseService;
import com.mylog.dto.category.CategoryCreateRequest;
import com.mylog.dto.category.CategoryDeleteRequest;
import com.mylog.dto.category.CategoryResponse;
import com.mylog.dto.category.CategoryUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    //카테고리 생성
    @PostMapping
    public CommonResult createCategory(
        @RequestBody @Valid CategoryCreateRequest request,
        @AuthenticationPrincipal CustomUser customUser
    ){
        categoryService.createCategory(request, customUser);
        return ResponseService.getSuccessResult();
    }

    //카테고리 조회
    @GetMapping
    public ListResult<CategoryResponse> getCategories(
        @AuthenticationPrincipal CustomUser customUser
    ){
        return ResponseService.getListResult(categoryService.getCategories(customUser));
    }

    //카테고리 수정
    @PutMapping
    public CommonResult updateCategory(
        @RequestBody @Valid CategoryUpdateRequest request,
        @AuthenticationPrincipal CustomUser customUser
    ){
        categoryService.updateCategory(request, customUser);
        return ResponseService.getSuccessResult();
    }

    //카테고리 삭제
    @DeleteMapping
    public CommonResult deleteCategory(
        @RequestBody @Valid CategoryDeleteRequest request,
        @AuthenticationPrincipal CustomUser customUser
    ) {
        categoryService.deleteCategory(request, customUser);
        return ResponseService.getSuccessResult();
    }

}
