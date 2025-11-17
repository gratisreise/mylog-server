package com.mylog.model.dto.article;


import com.mylog.model.entity.Article;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

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
