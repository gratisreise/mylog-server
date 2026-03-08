package com.mylog.domain.category.dto;

import com.mylog.domain.category.Category;
import lombok.Builder;

@Builder
public record CategoryResponse(Long id, String categoryName, Long memberId) {
  public static CategoryResponse from(Category category) {
    return CategoryResponse.builder()
        .id(category.getId())
        .categoryName(category.getCategoryName())
        .memberId(category.getMember().getId())
        .build();
  }
}
