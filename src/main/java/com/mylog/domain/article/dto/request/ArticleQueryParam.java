package com.mylog.domain.article.dto.request;

public record ArticleQueryParam(
    Long memberId, // null=전체, non-null=내 게시글
    String keyword, // 제목 검색 (선택)
    String tag, // 태그 필터 (선택)
    Long categoryId // 카테고리 필터 (선택)
    ) {
  public boolean hasMemberFilter() {
    return memberId != null;
  }

  public boolean hasKeyword() {
    return keyword != null && !keyword.isBlank();
  }

  public boolean hasTag() {
    return tag != null && !tag.isBlank();
  }

  public boolean hasCategory() {
    return categoryId != null;
  }
}
