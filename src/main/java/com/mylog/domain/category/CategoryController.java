<<<<<<<< HEAD:src/main/java/com/mylog/domain/category/CategoryController.java
<<<<<<<< HEAD:src/main/java/com/mylog/domain/category/CategoryController.java
package com.mylog.domain.category;

import com.mylog.auth.classes.CustomUser;
import com.mylog.category.dto.CategoryCreateRequest;
import com.mylog.category.dto.CategoryResponse;
import com.mylog.category.dto.CategoryUpdateRequest;
import com.mylog.common.response.CommonResult;
import com.mylog.common.response.ListResult;
import com.mylog.common.response.ResponseService;
import com.mylog.common.security.CustomUser;
import com.mylog.domain.category.dto.CategoryCreateRequest;
import com.mylog.domain.category.dto.CategoryResponse;
import com.mylog.domain.category.dto.CategoryUpdateRequest;
import com.mylog.domain.category.service.CategoryReader;
import com.mylog.domain.category.service.CategoryWriter;
import com.mylog.response.CommonResult;
import com.mylog.response.ListResult;
import com.mylog.response.ResponseService;
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
    private final CategoryService categoryService;

    //카테고리 생성
    @PostMapping
    @Operation(summary = "카테고리 생성")
    public CommonResult createCategory(
        @RequestBody @Valid CategoryCreateRequest request,
        @AuthenticationPrincipal CustomUser customUser
    ){
        categoryService.createCategory(request, customUser);
        return ResponseService.getSuccessResult();
    }

    //카테고리 조회
    @GetMapping
    @Operation(summary = "카테고리 목록 조회")
    public ListResult<CategoryResponse> getCategories(
        @AuthenticationPrincipal CustomUser customUser
    ){
        return ResponseService.getListResult(categoryService.getCategories(customUser));
    }

    //카테고리 수정
    @PutMapping("/{categoryId}")
    @Operation(summary = "카테고리 수정")
    public CommonResult updateCategory(
        @PathVariable Long categoryId,
        @RequestBody @Valid CategoryUpdateRequest request,
        @AuthenticationPrincipal CustomUser customUser
    ){
        categoryService.updateCategory(request, categoryId, customUser);
        return ResponseService.getSuccessResult();
    }

    //카테고리 삭제
    @DeleteMapping("/{categoryId}")
    @Operation(summary = "카테고리 삭제")
    public CommonResult deleteCategory(
        @PathVariable Long categoryId,
        @AuthenticationPrincipal CustomUser customUser
    ) {
        categoryService.deleteCategory(categoryId, customUser);
        return ResponseService.getSuccessResult();
    }

}
