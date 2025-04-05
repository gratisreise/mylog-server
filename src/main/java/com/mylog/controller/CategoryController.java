package com.mylog.controller;

import com.mylog.common.CommonResult;
import com.mylog.common.ListResult;
import com.mylog.common.ResponseService;
import com.mylog.dto.category.CategoryCreateRequest;
import com.mylog.dto.category.CategoryResponse;
import com.mylog.dto.category.CategoryUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.service.category.CategoryService;
import com.mylog.service.category.CategoryServiceFactory;
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
    private final CategoryServiceFactory factory;

    //카테고리 생성
    @PostMapping
    public CommonResult createCategory(
        @RequestBody @Valid CategoryCreateRequest request,
        @AuthenticationPrincipal CustomUser customUser
    ){
        CategoryService service = factory.getCategoryService(customUser.getProvider());
        service.createCategory(request, customUser);
        return ResponseService.getSuccessResult();
    }

    //카테고리 조회
    @GetMapping
    public ListResult<CategoryResponse> getCategories(
        @AuthenticationPrincipal CustomUser customUser
    ){
        CategoryService service = factory.getCategoryService(customUser.getProvider());
        return ResponseService.getListResult(service.getCategories(customUser));
    }

    //카테고리 수정
    @PutMapping
    public CommonResult updateCategory(
        @RequestBody @Valid CategoryUpdateRequest request,
        @AuthenticationPrincipal CustomUser customUser
    ){
        CategoryService service = factory.getCategoryService(customUser.getProvider());
        service.updateCategory(request, customUser);
        return ResponseService.getSuccessResult();
    }

    //카테고리 삭제
    @DeleteMapping("/{id}")
    public CommonResult deleteCategory(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUser customUser
    ) {
        CategoryService service = factory.getCategoryService(customUser.getProvider());
        service.deleteCategory(customUser, id);
        return ResponseService.getSuccessResult();
    }

}
