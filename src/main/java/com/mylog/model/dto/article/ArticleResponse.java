package com.mylog.model.dto.article;

import com.mylog.model.entity.Article;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public record ArticleResponse(
    Long id,
    String title,
    String content,
    String author,
    String category,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {


    public ArticleResponse(Article article, String author, String category) {
        this(article.getId(), article.getTitle(),
            article.getContent(), author, category,
            article.getCreatedAt(), article.getUpdatedAt());
    }

    public ArticleResponse(Article article) {
        this(article.getId(), article.getTitle(),
            article.getContent(), article.getMember().getNickname(),
            article.getCategory().getCategoryName(),
            article.getCreatedAt(), article.getUpdatedAt());
    }
}
