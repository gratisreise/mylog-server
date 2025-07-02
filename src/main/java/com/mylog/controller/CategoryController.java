package com.mylog.controller;

import com.mylog.common.CommonResult;
import com.mylog.common.ListResult;
import com.mylog.common.ResponseService;
import com.mylog.model.dto.category.CategoryCreateRequest;
import com.mylog.model.dto.category.CategoryResponse;
import com.mylog.model.dto.category.CategoryUpdateRequest;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.service.CategoryReadService;
import com.mylog.service.CategoryWriteService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryReadService categoryReadService;
    private final CategoryWriteService categoryWriteService;

    //카테고리 생성
    @PostMapping
    @Operation(summary = "카테고리 생성")
    public CommonResult createCategory(
        @RequestBody @Valid CategoryCreateRequest request,
        @AuthenticationPrincipal CustomUser customUser
    ){
        categoryWriteService.createCategory(request, customUser);
        return ResponseService.getSuccessResult();
    }

    //카테고리 조회
    @GetMapping
    @Operation(summary = "카테고리 목록 조회")
    public ListResult<CategoryResponse> getCategories(
        @AuthenticationPrincipal CustomUser customUser
    ){
        return ResponseService.getListResult(categoryReadService.getCategories(customUser));
    }

    //카테고리 수정
    @PutMapping("/{categoryId}")
    @Operation(summary = "카테고리 수정")
    public CommonResult updateCategory(
        @PathVariable Long categoryId,
        @RequestBody @Valid CategoryUpdateRequest request,
        @AuthenticationPrincipal CustomUser customUser
    ){
        categoryWriteService.updateCategory(request,categoryId, customUser);
        return ResponseService.getSuccessResult();
    }

    //카테고리 삭제
    @DeleteMapping("/{categoryId}")
    @Operation(summary = "카테고리 삭제")
    public CommonResult deleteCategory(
        @PathVariable Long categoryId,
        @AuthenticationPrincipal CustomUser customUser
    ) {
        categoryWriteService.deleteCategory(categoryId, customUser);
        return ResponseService.getSuccessResult();
    }

}