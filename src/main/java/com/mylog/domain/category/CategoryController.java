package com.mylog.domain.category;

import com.mylog.common.annotations.AuthenticatedMember;
import com.mylog.common.response.SuccessResponse;
import com.mylog.domain.category.dto.CategoryCreateRequest;
import com.mylog.domain.category.dto.CategoryResponse;
import com.mylog.domain.category.dto.CategoryUpdateRequest;
import com.mylog.domain.category.service.CategoryReader;
import com.mylog.domain.category.service.CategoryWriter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
  private final CategoryReader categoryReader;
  private final CategoryWriter categoryWriter;

  // 카테고리 생성
  @PostMapping
  @Operation(summary = "카테고리 생성")
  public ResponseEntity<SuccessResponse<Long>> createCategory(
      @RequestBody @Valid CategoryCreateRequest request, @AuthenticatedMember Long memberId) {
    Long categoryId = categoryWriter.createCategory(request, memberId);
    return SuccessResponse.toCreated(categoryId);
  }

  // 카테고리 조회
  @Operation(summary = "카테고리 목록 조회")
  @GetMapping
  public ResponseEntity<SuccessResponse<List<CategoryResponse>>> getCategories(
      @AuthenticatedMember Long memberId) {
    return SuccessResponse.toOk(categoryReader.getCategories(memberId));
  }

  // 카테고리 단일 조회
  @Operation(summary = "카테고리 단일 조회")
  @GetMapping("/{categoryId}")
  public ResponseEntity<SuccessResponse<CategoryResponse>> getCategory(
      @PathVariable Long categoryId, @AuthenticatedMember Long memberId) {
    return SuccessResponse.toOk(categoryReader.getCategory(categoryId, memberId));
  }

  // 카테고리 수정
  @Operation(summary = "카테고리 수정")
  @PutMapping("/{categoryId}")
  public ResponseEntity<SuccessResponse<Void>> updateCategory(
      @PathVariable Long categoryId,
      @RequestBody @Valid CategoryUpdateRequest request,
      @AuthenticatedMember Long memberId) {
    categoryWriter.updateCategory(request, categoryId, memberId);
    return SuccessResponse.toNoContent();
  }

  // 카테고리 삭제
  @Operation(summary = "카테고리 삭제")
  @DeleteMapping("/{categoryId}")
  public ResponseEntity<SuccessResponse<Void>> deleteCategory(
      @PathVariable Long categoryId, @AuthenticatedMember Long memberId) {
    categoryWriter.deleteCategory(categoryId, memberId);
    return SuccessResponse.toNoContent();
  }
}
