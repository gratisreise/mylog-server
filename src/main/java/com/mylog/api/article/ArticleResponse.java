package com.mylog.api.article;

import com.mylog.domain.entity.Article;
import java.time.LocalDateTime;
import java.util.List;

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
        this(article.getId(),
            article.getTitle(),
            article.getContent(),
            article.getMember().getNickname(),
            article.getCategory().getCategoryName(),
            article.getArticleImg(),
            tags,
            article.getCreatedAt(),
            article.getUpdatedAt());
    }

}