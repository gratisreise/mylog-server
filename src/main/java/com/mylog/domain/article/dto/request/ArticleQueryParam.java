package com.mylog.domain.article.dto.request;

public record ArticleQueryParam(
    Long memberId,
    String keyword,
    String tag,
    Long categoryId
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
