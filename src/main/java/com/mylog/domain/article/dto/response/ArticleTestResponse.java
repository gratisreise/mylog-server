package com.mylog.domain.article.dto.response;


import com.mylog.domain.article.entity.Article;
import java.time.LocalDateTime;

public record ArticleTestResponse(
    Long id,
    String title,
    String content,
    String author,
    String category,
    String articleImg,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ArticleTestResponse from(Article article){
        return new ArticleTestResponse(
            article.getId(),
            article.getTitle(),
            article.getContent(),
            article.getMember().getNickname(),
            article.getCategory().getCategoryName(),
            article.getArticleImg(),
            article.getCreatedAt(),
            article.getUpdatedAt()
        );
    }
}
