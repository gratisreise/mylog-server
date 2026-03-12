package com.mylog.domain.article.dto.response;

import com.mylog.domain.article.entity.Article;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record ArticleResponse(
    Long id,
    String title,
    String content,
    String author,
    String category,
    String articleImg,
    List<String> tags,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public ArticleResponse(Article article, List<String> tags) {
    this(
        article.getId(),
        article.getTitle(),
        article.getContent(),
        article.getMember().getNickname(),
        article.getCategory().getCategoryName(),
        article.getArticleImg(),
        tags,
        article.getCreatedAt(),
        article.getUpdatedAt());
  }

  public static ArticleResponse from(Article article) {
    return new ArticleResponse(article, List.of("친구", "생활", "게임"));
  }
}
