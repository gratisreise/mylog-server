package com.mylog.domain.article.dto.response;

public record ArticleCreateResponse(Long articleId) {
  public static ArticleCreateResponse from(Long articleId) {
    return new ArticleCreateResponse(articleId);
  }
}
